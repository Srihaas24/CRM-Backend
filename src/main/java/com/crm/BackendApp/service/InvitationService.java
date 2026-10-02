package com.crm.BackendApp.service;

import com.crm.BackendApp.dto.request.AcceptInviteRequest;
import com.crm.BackendApp.dto.request.InviteUserRequest;
import com.crm.BackendApp.dto.response.InviteUserResponse;
import com.crm.BackendApp.entity.Organization;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.entity.UserInvitation;
import com.crm.BackendApp.enums.UserStatus;
import com.crm.BackendApp.exception.BadRequestException;
import com.crm.BackendApp.repo.UserInvitationRepository;
import com.crm.BackendApp.repo.UserRepo;
import com.crm.BackendApp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final UserRepo userRepo;
    private final UserInvitationRepository userInvitationRepository;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.invitation-url:http://localhost:8080/accept-invite}")
    private String invitationUrl;

    @Transactional
    public InviteUserResponse inviteUser(InviteUserRequest request, CustomUserDetails loggedInUser) {
        Organization org = loggedInUser.getUser().getOrganization();
        if (org == null) {
            throw new BadRequestException("Logged-in admin must belong to an organization");
        }

        Long orgId = org.getId();
        String rateLimitKey = "invite:rate:limit:" + orgId;
        Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey);

        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(rateLimitKey, Duration.ofHours(1));
        }

        if (currentCount != null && currentCount > 20) {
            throw new BadRequestException("Rate limit exceeded. Maximum 20 invitations per hour per tenant.");
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepo.existsByEmail(email)) {
            throw new BadRequestException("User with this email already exists");
        }

        // Invalidate old pending tokens for this email
        List<UserInvitation> pendingInvites = userInvitationRepository.findByEmailAndAcceptedFalseAndInvalidatedFalse(email);
        for (UserInvitation invite : pendingInvites) {
            invite.setInvalidated(true);
        }
        userInvitationRepository.saveAll(pendingInvites);

        // Generate and save new invitation
        String token = UUID.randomUUID().toString();
        UserInvitation invitation = UserInvitation.builder()
                .email(email)
                .role(request.getRole())
                .organization(org)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .accepted(false)
                .invalidated(false)
                .build();

        UserInvitation saved = userInvitationRepository.save(invitation);

        // Send email invitation
        String link = invitationUrl + "?token=" + token;
        emailService.sendInvitationEmail(email, link);

        return InviteUserResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole())
                .token(saved.getToken())
                .status("PENDING")
                .expiresAt(saved.getExpiresAt())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public void acceptInvitation(AcceptInviteRequest request) {
        String token = request.getToken().trim();

        UserInvitation invitation = userInvitationRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid invitation token"));

        if (invitation.isAccepted()) {
            throw new BadRequestException("Invitation has already been accepted");
        }

        if (invitation.isInvalidated()) {
            throw new BadRequestException("Invitation is no longer valid");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invitation has expired");
        }

        if (userRepo.existsByEmail(invitation.getEmail())) {
            throw new BadRequestException("User with this email already exists");
        }

        // Create the new User
        User user = User.builder()
                .name(request.getName().trim())
                .email(invitation.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(invitation.getRole())
                .organization(invitation.getOrganization())
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();

        userRepo.save(user);

        // Mark invitation as accepted
        invitation.setAccepted(true);
        userInvitationRepository.save(invitation);
    }
}

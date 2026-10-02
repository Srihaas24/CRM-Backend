package com.crm.BackendApp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.BackendApp.dto.request.UploadUserRequest;
import com.crm.BackendApp.dto.response.DeactivateUserResponse;
import com.crm.BackendApp.dto.response.UploadUserResponse;
import com.crm.BackendApp.dto.response.UserResponse;
import com.crm.BackendApp.entity.Organization;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.enums.Role;
import com.crm.BackendApp.enums.UserStatus;
import com.crm.BackendApp.exception.BadRequestException;
import com.crm.BackendApp.exception.ResourceNotFoundException;
import com.crm.BackendApp.redis.RefreshTokenService;
import com.crm.BackendApp.redis.TokenDenylistService;
import com.crm.BackendApp.repo.ProjectMemberRepo;
import com.crm.BackendApp.repo.UserRepo;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.specification.UserSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepo userRepo;
	private final PasswordEncoder pe;
	private final ProjectMemberRepo projectMemberRepo;
	private final RefreshTokenService refreshTokenService;
	private final TokenDenylistService tokenDenylistService;
	private final AuditLogService auditLogService;

	public UploadUserResponse saveUser(UploadUserRequest uploadUserRequest, CustomUserDetails loggedInUser) {
		uploadUserRequest.setPassword(pe.encode(uploadUserRequest.getPassword()));

		Organization organization = loggedInUser.getUser().getOrganization();

		User user = User.builder()
				.name(uploadUserRequest.getName())
				.email(uploadUserRequest.getEmail())
				.password(uploadUserRequest.getPassword())
				.role(uploadUserRequest.getRole())
				.organization(organization)
				.userStatus(UserStatus.INVITED)
				.failedLoginAttempts(0)
				.build();

		User saved = userRepo.save(user);
		return UploadUserResponse.builder()
				.id(saved.getId())
				.name(saved.getName())
				.email(saved.getEmail())
				.role(saved.getRole())
				.build();
	}

	@Transactional
	public DeactivateUserResponse deactivateUser(Long userId, String reason, CustomUserDetails currentUser, String ipAddress) {
		Long orgId = currentUser.getUser().getOrganization().getId();

		// Tenant isolation
		User targetUser = userRepo.findByIdAndOrganizationId(userId, orgId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found in organization"));

		// 1. Self-deactivation guard
		if (currentUser.getId().equals(userId)) {
			throw new BadRequestException("You cannot deactivate your own account");
		}

		// 2. Tenant owner guard
		Organization org = targetUser.getOrganization();
		if (isTenantOwner(targetUser, org)) {
			throw new BadRequestException("Tenant owner cannot be deactivated");
		}

		// Already deactivated guard
		if (targetUser.getUserStatus() == UserStatus.INACTIVE || targetUser.getUserStatus() == UserStatus.DEACTIVATED) {
			throw new BadRequestException("User is already deactivated");
		}

		// Begin DB transaction updates
		LocalDateTime now = LocalDateTime.now();
		targetUser.setUserStatus(UserStatus.INACTIVE);
		targetUser.setDeletedAt(now);
		targetUser.setDeactivatedBy(currentUser.getId());
		targetUser.setDeactivationReason(reason);

		userRepo.save(targetUser);

		// Revoke ALL refresh tokens for this user
		refreshTokenService.deleteUserRefreshTokens(targetUser);

		// Add current access token to denylist
		tokenDenylistService.denylistUserTokens(targetUser.getId());

		// Remove from all project_members records
		projectMemberRepo.deleteByUserId(targetUser.getId());

		// Log USER_DEACTIVATED to audit log with reason
		auditLogService.log(targetUser, "USER_DEACTIVATED", ipAddress, reason);

		return DeactivateUserResponse.builder()
				.message("User deactivated successfully")
				.userId(targetUser.getId())
				.status(targetUser.getUserStatus())
				.deletedAt(targetUser.getDeletedAt())
				.deactivatedBy(targetUser.getDeactivatedBy())
				.deactivationReason(targetUser.getDeactivationReason())
				.build();
	}

	private boolean isTenantOwner(User user, Organization org) {
		if (org.getOrganizationEmail() != null && org.getOrganizationEmail().equalsIgnoreCase(user.getEmail())) {
			return true;
		}
		Optional<User> firstAdmin = userRepo.findFirstByOrganizationIdAndRoleOrderByIdAsc(org.getId(), Role.ADMIN);
		return firstAdmin.isPresent() && firstAdmin.get().getId().equals(user.getId());
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> getTenantUsers(User currentUser, UserStatus userStatus, Pageable pageable) {
		Long orgId = currentUser.getOrganization().getId();

		boolean isPm = currentUser.getRole() == Role.PROJECT_MANAGER;

		Specification<User> specification = UserSpecification.filterTenantUsers(orgId, userStatus, isPm);

		Page<User> usersPage = userRepo.findAll(specification, pageable);

		if (usersPage.isEmpty()) {
			return Page.empty(pageable);
		}

		// Batch compute project counts to avoid N+1 queries
		List<Long> userIds = usersPage.getContent().stream().map(User::getId).toList();
		Map<Long, Long> projectCounts = projectMemberRepo.countProjectsByUserIds(userIds)
				.stream()
				.collect(Collectors.toMap(
						row -> (Long) row[0],
						row -> (Long) row[1]));
		return usersPage
				.map(user -> mapToDto(user, currentUser.getRole(), projectCounts.getOrDefault(user.getId(), 0L)));
	}

	private UserResponse mapToDto(User user, Role callerRole, Long projectCount) {
		UserResponse.UserResponseBuilder builder = UserResponse.builder()
				.id(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.role(user.getRole())
				.status(user.getUserStatus())
				.projectCount(projectCount);

		if (callerRole == Role.ADMIN) {
			builder.lastLoginAt(user.getLastLoginAt())
					.lastLoginIp(user.getLastLoginIp())
					.failedLoginAttempts(user.getFailedLoginAttempts())
					.deletedAt(user.getDeletedAt())
					.deactivatedBy(user.getDeactivatedBy())
					.deactivationReason(user.getDeactivationReason());
		}

		return builder.build();
	}
}

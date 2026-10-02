package com.crm.BackendApp.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.crm.BackendApp.dto.request.RegisterRequest;
import com.crm.BackendApp.dto.response.RegisterResponse;
import com.crm.BackendApp.entity.EmailVerificationToken;
import com.crm.BackendApp.entity.Organization;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.mapper.AuthMapper;
import com.crm.BackendApp.repo.EmailVerificationTokenRepository;
import com.crm.BackendApp.repo.OrganizationRepo;
import com.crm.BackendApp.repo.UserRepo;
import com.crm.BackendApp.enums.UserStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterService 
{
	private final OrganizationRepo organizationRepo;
	
	private final UserRepo userRepo;
	
	private final AuthMapper authMapper;
	
	private final PasswordEncoder passwordEncoder;
	
	private final EmailVerificationTokenRepository emailVerificationTokenRepo;
	
	@Value("${app.verification-url}")
	private String verificationUrl;
	
	private final EmailService emailService;
	
	@Transactional
	public RegisterResponse regsiterOrganization(RegisterRequest request)
	{
		if(organizationRepo.existsByOrganizationEmail(request.getOrganizationEmail()))
		{
			throw new RuntimeException("Organization already exists");
		}
		
		if(userRepo.existsByEmail(request.getAdminEmail()))
		{
			throw new RuntimeException("Admin email already exists");
		}
		
		Organization organization = organizationRepo.save(authMapper.toOrganization(request));
		
		User user = userRepo.save(authMapper.toUser(request, organization, passwordEncoder.encode(request.getAdminPassword())));
		
		String token = UUID.randomUUID().toString();
		EmailVerificationToken verificationToken = EmailVerificationToken.builder()
				.user(user)
				.token(token)
				.expiresAt(LocalDateTime.now().plusMinutes(30))
				.createdAt(LocalDateTime.now())
				.used(false)
				.build();
				
		emailVerificationTokenRepo.save(verificationToken);
		
		String verificationLink = verificationUrl + "?token=" + token; 
		
		emailService.sendVerificationEmail(user.getEmail(), verificationLink);
		
		return new RegisterResponse("Organization registered successfully");
	}
	
	@Transactional
	public void verifyEmail(String token) 
	{
		EmailVerificationToken verificationToken = emailVerificationTokenRepo.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid verification token"));
		
		User user = verificationToken.getUser();
		if(user.isEmailVerified())
		{
			throw new RuntimeException("Email is already verified");
		}
		
		if(verificationToken.isUsed())
		{
			throw new RuntimeException("Verification token has already been used");
		}
		
		if(verificationToken.getExpiresAt().isBefore(LocalDateTime.now()))
		{
			throw new RuntimeException("Verfication has been expired");
		}
		
		user.setEmailVerified(true);
		if (user.getUserStatus() == UserStatus.INVITED) {
			user.setUserStatus(UserStatus.ACTIVE);
		}
		
		userRepo.save(user);
		
		verificationToken.setUsed(true);
		emailVerificationTokenRepo.save(verificationToken);
	}
}

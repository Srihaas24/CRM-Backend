package com.crm.BackendApp.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.exception.BadRequestException;
import com.crm.BackendApp.redis.RefreshTokenService;
import com.crm.BackendApp.repo.UserRepo;
import com.crm.BackendApp.enums.UserStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService 
{
	private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);
    private static final Duration RATE_LIMIT_TTL = Duration.ofHours(1);
    private static final int MAX_REQUESTS_PER_HOUR = 3;
	
	private final StringRedisTemplate redisTemplate;
	private final UserRepo userRepo;
	private final EmailService emailService;
	private final AuditLogService auditLogService; 
	private final SecureRandom secureRandom = new SecureRandom();
	
	private final PasswordEncoder passwordEncoder;
	
	private final RefreshTokenService refreshTokenService;
	
	@Value("${app.password-reset-url}")
    private String passwordResetUrl;	
	
	@Transactional
	public void forgotPassword(String email, String ipAddress) 
	{
		String normalizedEmail = email.trim().toLowerCase();
		
		String rateLimitKey = "password-reset:rate:" + normalizedEmail;
		
		Long requestCount = redisTemplate.opsForValue().increment(rateLimitKey);
		
		//Rate limiting before database lookup
		if(requestCount != null && requestCount == 1)
		{
			redisTemplate.expire(rateLimitKey, RATE_LIMIT_TTL);
		}
		if(requestCount!=null && requestCount > MAX_REQUESTS_PER_HOUR)
		{
			return;
		}
		
		//Check if email exists
		Optional<User> optionalUser = userRepo.findByEmail(normalizedEmail);
		if(optionalUser.isEmpty())
		{
			return;
		}
		
		User user = optionalUser.get();
		if (user.getUserStatus() == UserStatus.DEACTIVATED || user.getUserStatus() == UserStatus.INACTIVE) {
			return;
		}
		
		String token = generateSecureToken();
		
		String userTokenKey = "password-reset:user:" + user.getId();
		String previousToken = redisTemplate.opsForValue().get(userTokenKey);
		if(previousToken != null)
		{
			redisTemplate.delete("password-reset:token:" + previousToken);
		}
		
		String tokenKey =
                "password-reset:token:" + token;

        redisTemplate.opsForValue().set(
                tokenKey,
                user.getId().toString(),
                RESET_TOKEN_TTL
        );
        redisTemplate.opsForValue().set(
                userTokenKey,
                token,
                RESET_TOKEN_TTL
        );

        //Building the forgot password link
        String resetLink = passwordResetUrl + "?token=" + token;
        
        //Send an Password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        
        //Audit logging
        auditLogService.log(user, "PASSWORD_RESET_REQUESTED", ipAddress);
		
	}

	private String generateSecureToken() 
	{
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}

	@Transactional
	public void resetPassword(String token, String newPassword) 
	{
		String tokenKey = "password-reset:token:" + token;
		
		String userId = redisTemplate.opsForValue().get(tokenKey);
		
		//Token doesn't exist or has expired.
		if (userId == null) 
		{
			throw new BadRequestException("Invalid or expired password reset token");
	    }
		User user = userRepo.findById(Long.parseLong(userId))
				.orElseThrow(()->new BadRequestException("Invalid password reset token"));
		
		user.setPassword(passwordEncoder.encode(newPassword));
		if (user.getUserStatus() == UserStatus.INVITED) {
			user.setUserStatus(UserStatus.ACTIVE);
		}
		userRepo.save(user);
		
		redisTemplate.delete(tokenKey);
		redisTemplate.delete("password-reset:user:" + user.getId());
		
		refreshTokenService.deleteUserRefreshTokens(user);
	}
	
}

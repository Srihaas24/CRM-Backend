package com.crm.BackendApp.conroller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crm.BackendApp.dto.request.AcceptInviteRequest;
import com.crm.BackendApp.dto.request.ForgotPasswordRequest;
import com.crm.BackendApp.dto.request.LoginRequest;
import com.crm.BackendApp.dto.request.RegisterRequest;
import com.crm.BackendApp.dto.request.ResetPasswordRequest;
import com.crm.BackendApp.dto.response.RegisterResponse;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.enums.UserStatus;
import com.crm.BackendApp.exception.BadRequestException;
import com.crm.BackendApp.redis.AuthResponse;
import com.crm.BackendApp.redis.RefreshRequest;
import com.crm.BackendApp.redis.RefreshTokenService;
import com.crm.BackendApp.repo.UserRepo;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.security.jwt.JwtService;
import com.crm.BackendApp.service.InvitationService;
import com.crm.BackendApp.service.PasswordResetService;
import com.crm.BackendApp.service.RegisterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/v1/auth", "/auth"})
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	
	private final JwtService jwtService;
	
	private final RefreshTokenService refreshTokenService;
	
	private final UserRepo userRepo;
	
	private final RegisterService registerService;
	
	private final PasswordResetService passwordResetService;

	private final InvitationService invitationService;

	@PostMapping("/accept-invite")
	public ResponseEntity<String> acceptInvitation(@Valid @RequestBody AcceptInviteRequest request) {
		invitationService.acceptInvitation(request);
		return ResponseEntity.ok("Invitation accepted successfully");
	}

	@PostMapping("/login")
	public AuthResponse login(@RequestBody LoginRequest lr, HttpServletRequest request) {
		if (lr == null || lr.getEmail() == null || lr.getPassword() == null) {
			throw new BadRequestException("Email and password are required");
		}

		String email = lr.getEmail().trim().toLowerCase();
		String ipAddress = request.getRemoteAddr();
		
		Optional<User> optionalUser = userRepo.findByEmail(email);
		if (optionalUser.isEmpty()) {
			throw new BadRequestException("Invalid credentials");
		}
		
		User user = optionalUser.get();
		if (user.getUserStatus() == UserStatus.DEACTIVATED || user.getUserStatus() == UserStatus.INACTIVE) {
			throw new BadRequestException("User account is deactivated");
		}
		
		try {
			Authentication a = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, lr.getPassword()));
			
			user.setLastLoginAt(LocalDateTime.now());
			user.setLastLoginIp(ipAddress);
			user.setFailedLoginAttempts(0);
			
			if (user.getUserStatus() == UserStatus.INVITED) {
				user.setUserStatus(UserStatus.ACTIVE);
			}
			
			userRepo.save(user);
			
			CustomUserDetails userDetails = (CustomUserDetails) a.getPrincipal();
			String accessToken = jwtService.generateAccessToken(userDetails.getUsername());
			return new AuthResponse(accessToken, refreshTokenService.createRefreshToken(user));
		} catch (Exception e) {
			int currentAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
			user.setFailedLoginAttempts(currentAttempts + 1);
			userRepo.save(user);
			throw new BadRequestException("Invalid credentials");
		}
	}
	
	@PostMapping("/refresh")
	public AuthResponse refresh(@RequestBody RefreshRequest rr)
	{
		String refreshToken = rr.getRefreshToken();
		
		String id = refreshTokenService.validateRefreshToken(refreshToken);
		if(id == null)
		{
			throw new RuntimeException("Invalid Refresh Token");
		}
		
		User user = userRepo.findById(Long.parseLong(id)).orElseThrow();
		
		if (user.getUserStatus() == UserStatus.DEACTIVATED || user.getUserStatus() == UserStatus.INACTIVE) {
			throw new BadRequestException("User account is deactivated");
		}
		
		//Delete old Refresh Token
		refreshTokenService.deleteRefreshToken(refreshToken);
		
		//Create new Access Token and new Refresh Token
		String newAccessToken = jwtService.generateAccessToken(user.getEmail());
		String newRefreshToken = refreshTokenService.createRefreshToken(user);
		
		return new AuthResponse(newAccessToken, newRefreshToken);
	}
	
	@PostMapping("/logout")
	public String logout(@RequestBody RefreshRequest rr)
	{
		refreshTokenService.deleteRefreshToken(rr.getRefreshToken());
		return "Logged out";
	}
	
	@PostMapping("/register/organization")
	public RegisterResponse registerOrganization(@Valid @RequestBody RegisterRequest registerRequest)
	{
		return registerService.regsiterOrganization(registerRequest);
	}
	
	@GetMapping("/verify-email")
	public ResponseEntity<String> verifyEmail(@RequestParam String token)
	{
		registerService.verifyEmail(token);
		return ResponseEntity.ok("Email verified successfully");
	}
	
	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpServletRequest)
	{
		String ipAddress = httpServletRequest.getRemoteAddr();
		
		passwordResetService.forgotPassword(request.getEmail(), ipAddress);
		return ResponseEntity.ok("If email exists, password reset email has been sent");
	}
	
	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request)
	{
		passwordResetService.resetPassword(request.getToken(),request.getNewPassword());
		
		return ResponseEntity.ok("Password reset successfully");
	}
}

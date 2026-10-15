package com.crm.BackendApp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService 
{
	private final JavaMailSender javaMailSender;

	@Value("${spring.mail.username:srihaas24@gmail.com}")
	private String fromEmail;
	
	@Async
	public void sendVerificationEmail(String recipientEmail, String verificationUrl)
	{
		try {
			SimpleMailMessage message = new SimpleMailMessage();

			message.setFrom(fromEmail);
			message.setTo(recipientEmail);
			message.setSubject("Verify your CRM account");

			message.setText(
					"Welcome to our CRM.\n\n" +
					"Please verify your email address by clicking the link below:\n\n" +
					verificationUrl +
					"\n\n" +
					"This link will expire in 30 minutes.\n\n" +
					"If you did not create this account, you can ignore this email."
			);

			javaMailSender.send(message);
			log.info("Verification email successfully sent to {}", recipientEmail);
		} catch (Exception e) {
			log.error("Failed to send verification email to {}: {}", recipientEmail, e.getMessage(), e);
		}
	}

	@Async
	public void sendPasswordResetEmail(String email, String resetLink) 
	{
		try {
			SimpleMailMessage message = new SimpleMailMessage();

			message.setFrom(fromEmail);
			message.setTo(email);
			message.setSubject("Password Reset Request");

			message.setText(
					"You requested a password reset.\n\n" +
					"Click the link below to reset your password:\n\n" +
					resetLink +
					"\n\n" +
					"This link will expire in 15 minutes.\n\n" +
					"If you did not request this, you can safely ignore this email."
			);

			javaMailSender.send(message);
			log.info("Password reset email successfully sent to {}", email);
		} catch (Exception e) {
			log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
		}
	}

	@Async
	public void sendInvitationEmail(String recipientEmail, String invitationUrl)
	{
		try {
			SimpleMailMessage message = new SimpleMailMessage();

			message.setFrom(fromEmail);
			message.setTo(recipientEmail);
			message.setSubject("Invitation to join the CRM organization");

			message.setText(
					"You have been invited to join the CRM organization.\n\n" +
					"Please accept the invitation by clicking the link below:\n\n" +
					invitationUrl +
					"\n\n" +
					"This link will expire in 24 hours.\n\n" +
					"If you did not expect this invitation, you can ignore this email."
			);

			javaMailSender.send(message);
			log.info("Invitation email successfully sent to {}", recipientEmail);
		} catch (Exception e) {
			log.error("Failed to send invitation email to {}: {}", recipientEmail, e.getMessage(), e);
		}
	}
}

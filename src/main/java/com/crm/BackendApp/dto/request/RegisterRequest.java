package com.crm.BackendApp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
	@Email
	private String organizationEmail;

	@NotBlank
	private String organizationName;
	private String phone;

	@NotBlank
	private String adminName;

	@Email
	private String adminEmail;

	@Size(min = 8)
	private String adminPassword;
}

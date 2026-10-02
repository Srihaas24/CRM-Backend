package com.crm.BackendApp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClientRequest 
{
	@NotBlank
	private String name;
	
	@Email
	private String email;
	
	@NotBlank
	private String password;
}

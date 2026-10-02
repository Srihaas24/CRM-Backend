package com.crm.BackendApp.dto.request;

import com.crm.BackendApp.enums.Role;

import lombok.Data;

@Data
public class UploadUserRequest 
{	
	private String name;
	
	private String email;
	private String password;
	
	private Role role;
}

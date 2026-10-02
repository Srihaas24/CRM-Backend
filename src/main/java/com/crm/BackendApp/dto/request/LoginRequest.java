package com.crm.BackendApp.dto.request;

import lombok.Data;

@Data
public class LoginRequest 
{
	private String email;
    private String password;
}

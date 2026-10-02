package com.crm.BackendApp.dto.response;

import com.crm.BackendApp.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientResponse 
{
	private String name;
	private String email;
	private Role role;
	private String organizationName;
}

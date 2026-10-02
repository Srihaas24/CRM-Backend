package com.crm.BackendApp.mapper;


import org.springframework.stereotype.Component;

import com.crm.BackendApp.dto.request.RegisterRequest;
import com.crm.BackendApp.entity.Organization;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.enums.Role;

@Component
public class AuthMapper 
{
	public Organization toOrganization(RegisterRequest req)
	{
		return Organization.builder()
				.organizationEmail(req.getOrganizationEmail())
				.organizationName(req.getOrganizationName())
				.phone(req.getPhone())
				.build();
	}
	
	public User toUser(RegisterRequest request, Organization organization, String encodedPassword)
	{
		return User.builder()
				.name(request.getAdminName())
				.email(request.getAdminEmail())
				.password(encodedPassword)
				.role(Role.ADMIN)
				.organization(organization)
				.build();
	}
}

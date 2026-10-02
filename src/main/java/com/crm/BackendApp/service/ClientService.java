package com.crm.BackendApp.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.BackendApp.dto.request.CreateClientRequest;
import com.crm.BackendApp.dto.response.ClientResponse;
import com.crm.BackendApp.entity.Organization;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.enums.Role;
import com.crm.BackendApp.repo.UserRepo;
import com.crm.BackendApp.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService 
{
	public final UserRepo userRepo;
	
	public final PasswordEncoder passwordEncoder;
	
	@Transactional
	public ClientResponse createClient(CreateClientRequest clientRequest, CustomUserDetails loggedInUser)
	{
		
		if(userRepo.existsByEmail(clientRequest.getEmail()))
		{
			throw new RuntimeException("Email already exists");
		}
		
		Organization org = loggedInUser.getUser().getOrganization();
		
		User client = User.builder()
				.name(clientRequest.getName())
				.email(clientRequest.getEmail())
				.password(passwordEncoder.encode(clientRequest.getPassword()))
				.role(Role.CLIENT)
				.organization(org)
				.build();
		
		userRepo.save(client);
		
		return ClientResponse.builder()
				.name(client.getName())
				.email(client.getEmail())
				.role(client.getRole())
				.organizationName(org.getOrganizationName())
				.build();
				
	}
}

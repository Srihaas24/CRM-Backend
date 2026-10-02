package com.crm.BackendApp.conroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crm.BackendApp.dto.request.CreateClientRequest;
import com.crm.BackendApp.dto.response.ClientResponse;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.service.ClientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController 
{
	public final ClientService clientService;
	
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
	public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody CreateClientRequest clientRequest, Authentication authentication)
	{
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
		
		ClientResponse response = clientService.createClient(clientRequest,customUserDetails);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}

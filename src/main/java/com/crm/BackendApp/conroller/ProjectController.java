package com.crm.BackendApp.conroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crm.BackendApp.dto.request.ProjectRequest;
import com.crm.BackendApp.dto.request.ProjectUpdateRequest;
import com.crm.BackendApp.dto.response.ProjectResponse;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController 
{
	public final ProjectService projectService;
	
	//Add Project
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER','DEVELOPER','CLIENT')")
	@PostMapping
	public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest projectRequest, Authentication authentication)
	{
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
		
		ProjectResponse projectResponse = projectService.createProject(projectRequest, customUserDetails);
		 
		return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);
	}
	
	//Get list of Projects
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
	@GetMapping
	public ResponseEntity<List<ProjectResponse>> getProjects(Authentication authentication)
	{
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
		
		return ResponseEntity.ok(projectService.getProjects(customUserDetails));
	}
	
	//Get a specific Project by ID
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER','DEVELOPER','CLIENT')")
	@GetMapping("/{id}")
	public ResponseEntity<ProjectResponse> getProject(@PathVariable("id") Long projectId, Authentication authentication)
	{
		CustomUserDetails customUserDetails = (CustomUserDetails)  authentication.getPrincipal();
		
		ProjectResponse projectResponse = projectService.getProjectById(customUserDetails, projectId);
		
		return ResponseEntity.ok(projectResponse);
	}
	
	//Update a Project details
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER','DEVELOPER','CLIENT')")
	@PutMapping("/{id}")
	public ResponseEntity<ProjectResponse> updateProject(
			@PathVariable("id") Long projectId, 
			@RequestBody ProjectUpdateRequest projectUpdateRequest, 
			Authentication authentication)
	{
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal(); 
		
		ProjectResponse projectResponse = projectService.updateProject(projectId, projectUpdateRequest, customUserDetails);
		
		return ResponseEntity.ok(projectResponse);
	}
}

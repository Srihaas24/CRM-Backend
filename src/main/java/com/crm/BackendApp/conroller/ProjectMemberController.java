package com.crm.BackendApp.conroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crm.BackendApp.dto.request.AddMemberRequest;
import com.crm.BackendApp.dto.response.ProjectMemberResponse;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.service.ProjectMemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects/{id}/members")
@RequiredArgsConstructor
public class ProjectMemberController 
{
	private final ProjectMemberService projectMemberService; 
	
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
	public ResponseEntity<ProjectMemberResponse> addProjectMember(
			@PathVariable("id") Long projectId, 
			@RequestBody AddMemberRequest addMemberRequest, 
			Authentication authentication)
	{
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		
		ProjectMemberResponse projectMemberResponse = projectMemberService.addProjectMember(projectId,addMemberRequest,customUserDetails);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(projectMemberResponse);
	}
	
	@GetMapping
	public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(@PathVariable("id") Long projectId, Authentication authentication)
	{
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		
		return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId, customUserDetails));
	}
	
	@DeleteMapping("/{uid}")
	public ResponseEntity<String> deleteProjectMember(
			@PathVariable("id") Long projectId, 
			@PathVariable("uid") Long userId, 
			Authentication authentication)
	{
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		
		projectMemberService.deleteProjectMember(projectId, userId, customUserDetails);
		
		return ResponseEntity.ok("Project Member removed successfully");
	}
}

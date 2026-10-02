package com.crm.BackendApp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.BackendApp.dto.request.AddMemberRequest;
import com.crm.BackendApp.dto.response.ProjectMemberResponse;
import com.crm.BackendApp.entity.Project;
import com.crm.BackendApp.entity.ProjectMember;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.exception.ResourceNotFoundException;
import com.crm.BackendApp.exception.BadRequestException;
import com.crm.BackendApp.enums.UserStatus;
import com.crm.BackendApp.repo.ProjectMemberRepo;
import com.crm.BackendApp.repo.ProjectRepo;
import com.crm.BackendApp.repo.UserRepo;
import com.crm.BackendApp.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectMemberService 
{
	private final ProjectRepo projectRepo;
	
	private final UserRepo userRepo;
	
	private final ProjectMemberRepo projectMemberRepo;
	
	@Transactional
	public ProjectMemberResponse addProjectMember(Long projectId, AddMemberRequest addMemberRequest, CustomUserDetails loggedInUser)
	{
		Long orgId = loggedInUser.getUser().getOrganization().getId();
		
		System.out.println("Logged-in Org ID: " + orgId);
		System.out.println("Requested User ID: " + addMemberRequest.getUserId());
		
		Project project = projectRepo.findByOrganizationIdAndId(orgId, projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		
		User user = userRepo.findByIdAndOrganizationId(addMemberRequest.getUserId(), orgId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		
		if (user.getUserStatus() == UserStatus.DEACTIVATED || user.getUserStatus() == UserStatus.INACTIVE) {
			throw new BadRequestException("Cannot add a deactivated user to a project");
		}
		
		if(projectMemberRepo.existsByProjectIdAndUserId(projectId, addMemberRequest.getUserId()))
		{
			throw new IllegalArgumentException("User is already assigned");
		}
		
		ProjectMember projectMember = ProjectMember.builder()
				.project(project)
				.user(user)
				.assignedDate(LocalDate.now())
				.build();
		
		ProjectMember saved = projectMemberRepo.save(projectMember);
		
		User savedUser = saved.getUser();
		
		return ProjectMemberResponse.builder()
				.userId(savedUser.getId())
				.name(savedUser.getName())
				.email(savedUser.getEmail())
				.role(savedUser.getRole())
				.build();
	}
	
	public List<ProjectMemberResponse> getProjectMembers(Long projectId, CustomUserDetails loggedInUser)
	{
		Long orgId = loggedInUser.getUser().getOrganization().getId();
		
		List<ProjectMember> projectMembers = projectMemberRepo.findByProjectIdAndProjectOrganizationId(projectId, orgId);
		
		return projectMembers.stream()
				.map(projectMember -> ProjectMemberResponse.builder()
						.userId(projectMember.getUser().getId())
						.name(projectMember.getUser().getName())
						.email(projectMember.getUser().getEmail())
						.role(projectMember.getUser().getRole())
						.build()
						)
				.toList();
	}

	@Transactional
	public boolean deleteProjectMember(Long projectId, Long userId, CustomUserDetails loggedInUser) 
	{
		Long orgId = loggedInUser.getUser().getOrganization().getId();
		Long deleted = projectMemberRepo.deleteByProjectOrganizationIdAndProjectIdAndUserId(orgId, projectId, userId);
		
		if(deleted == 0)
		{
			throw new ResourceNotFoundException("Project member not found");
		}
		return true;
	}
}

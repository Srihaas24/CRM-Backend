package com.crm.BackendApp.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.crm.BackendApp.dto.request.ProjectRequest;
import com.crm.BackendApp.dto.request.ProjectUpdateRequest;
import com.crm.BackendApp.dto.response.ProjectResponse;
import com.crm.BackendApp.entity.Organization;
import com.crm.BackendApp.entity.Project;
import com.crm.BackendApp.enums.ProjectStatus;
import com.crm.BackendApp.exception.BadRequestException;
import com.crm.BackendApp.exception.ResourceNotFoundException;
import com.crm.BackendApp.mapper.ProjectResponseMapper;
import com.crm.BackendApp.repo.ProjectRepo;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.service.validation.ProjectStatusValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService 
{
	public final ProjectRepo projectRepo;
	
	public final ProjectResponseMapper projectResponseMapper;
	
	@CacheEvict(value = "organization_projects", key = "#loggedInUser.user.organization.id")
	public ProjectResponse createProject(ProjectRequest projectRequest, CustomUserDetails loggedInUser)
	{
		Organization org = loggedInUser.getUser().getOrganization();
		
		Project project = Project.builder()
				.projectName(projectRequest.getProjectName())
				.description(projectRequest.getDescription())
				.endDate(projectRequest.getEndDate())
				.startDate(projectRequest.getStartDate())
				.projectStatus(ProjectStatus.PLANNING)
				.organization(org)
				.build();
		
		Project saved = projectRepo.save(project);
		
		return projectResponseMapper.toProjectResponse(saved);
	}
	
	@Cacheable(value = "organization_projects", key = "#loggedInUser.user.organization.id")
	public List<ProjectResponse> getProjects(CustomUserDetails loggedInUser)
	{
		List<Project> projects = projectRepo.findByOrganizationId(loggedInUser.getUser().getOrganization().getId());
		
		return projects.stream()
				.map(project->projectResponseMapper.toProjectResponse(project))
				.toList();
	}
	
	@Cacheable(value = "projects", key = "#loggedInUser.user.organization.id + ':' + #projectId")
	public ProjectResponse getProjectById(CustomUserDetails loggedInUser, Long projectId)
	{
		
		Project project = projectRepo.findByOrganizationIdAndId(loggedInUser.getUser().getOrganization().getId(), projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		
		 return projectResponseMapper.toProjectResponse(project);
	}
	
	@Caching(evict = {
		@CacheEvict(value = "projects", key = "#loggedInUser.user.organization.id + ':' + #projectId"),
		@CacheEvict(value = "organization_projects", key = "#loggedInUser.user.organization.id")
	})
	public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest projectUpdateRequest, CustomUserDetails loggedInUser)
	{
		//Project Existence
		Project project = projectRepo.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		
		//Organization validation
		if(!project.getOrganization().getId()
				.equals(loggedInUser.getUser().getOrganization().getId()))
		{
			throw new AccessDeniedException("The project does not belong to your organization");
		}
		
		//Deadline Validation
		if(projectUpdateRequest.getEndDate().isBefore(projectUpdateRequest.getStartDate()))
		{
			throw new BadRequestException("End date must be after start date");
		}
		
		//Status Validation
		if(ProjectStatusValidator.isValid(project.getProjectStatus(), projectUpdateRequest.getProjectStatus()))
		{
			throw new BadRequestException("Invalid Project status transition");
		}
		
		project.setProjectName(projectUpdateRequest.getProjectName());
		project.setDescription(projectUpdateRequest.getDescription());
		project.setEndDate(projectUpdateRequest.getEndDate());
		project.setStartDate(projectUpdateRequest.getStartDate());
		project.setProjectStatus(projectUpdateRequest.getProjectStatus());
		
		Project saved = projectRepo.save(project);
		
		return projectResponseMapper.toProjectResponse(saved);
	}
}

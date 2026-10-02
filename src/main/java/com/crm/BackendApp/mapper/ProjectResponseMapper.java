package com.crm.BackendApp.mapper;

import org.springframework.stereotype.Component;

import com.crm.BackendApp.dto.response.ProjectResponse;
import com.crm.BackendApp.entity.Project;

@Component
public class ProjectResponseMapper 
{
	public ProjectResponse toProjectResponse(Project project)
	{
		return ProjectResponse.builder()
				.description(project.getDescription())
				.projectName(project.getProjectName())
				.startDate(project.getStartDate())
				.endDate(project.getEndDate())
				.projectStatus(project.getProjectStatus())
				.build();
	}
}

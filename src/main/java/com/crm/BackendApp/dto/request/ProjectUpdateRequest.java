package com.crm.BackendApp.dto.request;

import java.time.LocalDate;

import com.crm.BackendApp.enums.ProjectStatus;

import lombok.Data;

@Data
public class ProjectUpdateRequest 
{
	private String projectName;
	private String description;
	private LocalDate startDate;
	private LocalDate endDate;
	private ProjectStatus projectStatus;
}

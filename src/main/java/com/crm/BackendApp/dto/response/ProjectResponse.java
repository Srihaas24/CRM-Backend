package com.crm.BackendApp.dto.response;

import java.time.LocalDate;

import com.crm.BackendApp.enums.ProjectStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse 
{
	private String projectName;
	private String description;
	private LocalDate startDate;
	private LocalDate endDate;
	private ProjectStatus projectStatus;
}

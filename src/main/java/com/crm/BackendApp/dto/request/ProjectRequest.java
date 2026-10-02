package com.crm.BackendApp.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProjectRequest 
{
	private String projectName;
	private String description;
	private LocalDate startDate;
	private LocalDate endDate;
}

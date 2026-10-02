package com.crm.BackendApp.dto.request;

import java.time.LocalDate;

import com.crm.BackendApp.enums.TaskPriority;
import com.crm.BackendApp.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTaskRequest 
{
	@NotBlank
	private String title; 
	
	private String description;
	
	private LocalDate dueDate;
	
	@NotNull
	private Long assignedToUserId;
	
	private TaskStatus taskStatus;
	private TaskPriority taskPriority;
}

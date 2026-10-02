package com.crm.BackendApp.dto.response;

import java.time.LocalDate;

import com.crm.BackendApp.enums.TaskPriority;
import com.crm.BackendApp.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskDetails 
{
	private Long id;
	
	private String title; 
	
	private String description;
	
	private TaskStatus taskStatus;
	
	private TaskPriority taskPriority;
	
	private LocalDate dueDate;
	
	private Long projectId;
	
	private String projectName;

	private Long assignedToUserId;
	
	private String assignedToUserName;
	
	private String assignedToUserEmail;
}

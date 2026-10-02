package com.crm.BackendApp.entity;

import java.time.LocalDate;

import com.crm.BackendApp.enums.TaskPriority;
import com.crm.BackendApp.enums.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name="task")

public class Task 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private String title; 
	
	private String description;
	
	@Enumerated(EnumType.STRING)
	private TaskStatus taskStatus;
	
	@Enumerated(EnumType.STRING)
	private TaskPriority taskPriority;
	
	private LocalDate dueDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="project_id", nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="assigned_to_user", nullable=false)
	private User assignedTo;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="assigned_by_user", nullable=false)
	private User assignedBy;
	
}

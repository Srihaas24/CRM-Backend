package com.crm.BackendApp.entity;

import java.time.LocalDate;

import com.crm.BackendApp.enums.ProjectStatus;

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
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@Table(name = "projects")
@NoArgsConstructor
@AllArgsConstructor
public class Project 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(insertable=false, updatable=false)
	private Long id;
	
	@Column(nullable = false)
	private String projectName;
	
	@Column(length = 1000)
	private String description;
	
	private LocalDate startDate;
	
	private LocalDate endDate;
	
	@Enumerated(EnumType.STRING)
	private ProjectStatus projectStatus;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id" , nullable = false )
	private Organization organization;
}

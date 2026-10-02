package com.crm.BackendApp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "audit_logs") 
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class AuditLog 
{
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY) 
	@JoinColumn(name = "user_id", nullable = false) 
	private User user;  
	
	//What happened * * Example: * PASSWORD_RESET_REQUESTED * PASSWORD_RESET_COMPLETED * LOGIN_SUCCESS * LOGIN_FAILED
	@Column(nullable = false, length = 100) 
	private String action; 
	
	@Column(name = "ip_address", length = 45) 
	private String ipAddress; 

	@Column(length = 500)
	private String reason; 
	
	@Column(name = "created_at", nullable = false) 
	private LocalDateTime createdAt; 
	
	@PrePersist 
	protected void onCreate() 
	{ 
		createdAt = LocalDateTime.now(); 
	}
}

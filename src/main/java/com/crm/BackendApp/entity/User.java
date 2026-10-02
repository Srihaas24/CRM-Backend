package com.crm.BackendApp.entity;



import java.time.LocalDateTime;

import com.crm.BackendApp.enums.Role;
import com.crm.BackendApp.enums.UserStatus;

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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
@Builder
public class User 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	@Column(unique=true,nullable=false)
	private String email;
	private String password;
	
	@Enumerated(EnumType.STRING)
	private Role role;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization; 
	
	@Column(nullable=false)
	@Builder.Default
	private boolean emailVerified = false;
	
	
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus userStatus = UserStatus.ACTIVE;
	
	private LocalDateTime lastLoginAt;
    private String lastLoginIp;

    @Builder.Default
    private Integer failedLoginAttempts = 0;

    private LocalDateTime deletedAt;
    private Long deactivatedBy;
    private String deactivationReason;
}



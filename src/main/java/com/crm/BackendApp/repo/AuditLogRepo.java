package com.crm.BackendApp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crm.BackendApp.entity.AuditLog;

@Repository
public interface AuditLogRepo extends JpaRepository<AuditLog, Long> 
{
	
}

package com.crm.BackendApp.service;

import org.springframework.stereotype.Service;

import com.crm.BackendApp.entity.AuditLog;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.repo.AuditLogRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService 
{
	
	public final AuditLogRepo auditLogRepo;
	
	public void log(User user, String action, String ipAddress) 
	{
		log(user, action, ipAddress, null);
	}

	public void log(User user, String action, String ipAddress, String reason) 
	{
		AuditLog auditLog = AuditLog.builder()
		.user(user)
		.action(action)
		.ipAddress(ipAddress)
		.reason(reason)
		.build();
		
		auditLogRepo.save(auditLog);
	}
}

package com.crm.BackendApp.service.validation;

import java.util.Map;
import java.util.Set;

import com.crm.BackendApp.enums.ProjectStatus;

public class ProjectStatusValidator 
{
	public static final Map<ProjectStatus, Set<ProjectStatus>> ALLOWED = 
			Map.of(
					ProjectStatus.PLANNING,
					Set.of(
							ProjectStatus.IN_PROGRESS,
							ProjectStatus.ON_HOLD,
							ProjectStatus.CANCELLED,
							ProjectStatus.COMPLETED
							),
					
					ProjectStatus.IN_PROGRESS,
					Set.of(
							ProjectStatus.ON_HOLD,
							ProjectStatus.COMPLETED,
							ProjectStatus.CANCELLED
							),
					
					ProjectStatus.ON_HOLD,
					Set.of(
							ProjectStatus.COMPLETED,
							ProjectStatus.CANCELLED
							),
					
					ProjectStatus.COMPLETED,
					Set.of(),
					
					ProjectStatus.CANCELLED,
					Set.of()
					);
	
	public static boolean isValid(ProjectStatus current, ProjectStatus next)
	{
		if(current == next) return true;
		
		return ALLOWED.getOrDefault(next, Set.of()).contains(next);
	}
}

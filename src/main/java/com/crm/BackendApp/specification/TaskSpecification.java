package com.crm.BackendApp.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.crm.BackendApp.entity.ProjectMember;
import com.crm.BackendApp.entity.Task;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.enums.Role;
import com.crm.BackendApp.enums.TaskStatus;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class TaskSpecification {

	public static Specification<Task> taskFilter(User currentUser, Long projectId, Long assignedUserId, TaskStatus status) 
	{
		return (root,query,cb) -> {
			List<Predicate> predicate = new ArrayList<>();
			
			//Tenant isolation
			predicate.add(cb.equal(root.get("project").get("organization").get("id"), currentUser.getOrganization().getId()));
			
			//Role based project details visibility
			if(currentUser.getRole() == Role.PROJECT_MANAGER || currentUser.getRole() == Role.DEVELOPER)
			{
				Subquery<Long> subQuery = query.subquery(Long.class);
				Root<ProjectMember> pmRoot = subQuery.from(ProjectMember.class);
				
				subQuery.select(pmRoot.get("project").get("id")).where(cb.equal(pmRoot.get("user").get("id"), currentUser.getId()));
				
				Path<Long> projectIdPath = root.get("project").get("id");
				predicate.add(projectIdPath.in(subQuery));
			}
			
			//All tasks of a specific project
			if(projectId!=null)
			{
				predicate.add(cb.equal(root.get("project").get("id"), projectId));
			}
			
			if(assignedUserId!=null)
			{
				predicate.add(cb.equal(root.get("assignedTo").get("id"), assignedUserId));
			}
			
			if(status!=null)
			{
				predicate.add(cb.equal(root.get("taskStatus"), status));
			}
			
			return cb.and(predicate.toArray(new Predicate[0]));
		};
	}

}

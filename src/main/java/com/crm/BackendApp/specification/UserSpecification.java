package com.crm.BackendApp.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.enums.UserStatus;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification 
{
	public static Specification<User> filterTenantUsers(Long orgId, UserStatus statusFilter, boolean forceActiveOnly)
	{
		return (root,query, cb)->{
			List<Predicate> predicate = new ArrayList<>();
			
			//Tenant isolation
			predicate.add(cb.equal(root.get("organization").get("id"), orgId));
			
			if(forceActiveOnly)
			{
				predicate.add(cb.equal(root.get("userStatus"), UserStatus.ACTIVE));
			}
			else
			{
				predicate.add(cb.equal(root.get("userStatus"), statusFilter));
			}
			
			return cb.and(predicate.toArray(new Predicate[0]));
		};
	}
}

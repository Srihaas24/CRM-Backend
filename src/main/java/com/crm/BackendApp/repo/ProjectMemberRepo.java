package com.crm.BackendApp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crm.BackendApp.entity.ProjectMember;

@Repository
public interface ProjectMemberRepo extends JpaRepository<ProjectMember,Long> 
{
	boolean existsByProjectIdAndUserId(Long projectId, Long userId);
	
	List<ProjectMember> findByProjectIdAndProjectOrganizationId(Long projectId, Long organizationId);
	
	Long deleteByProjectOrganizationIdAndProjectIdAndUserId(Long orgId, Long projectId, Long userId);
	
	@Query("SELECT pm.user.id, COUNT(pm.project.id) FROM ProjectMember pm WHERE pm.user.id IN :userIds GROUP BY pm.user.id")
	List<Object[]> countProjectsByUserIds(@Param("userIds") List<Long> userIds);

	void deleteByUserId(Long userId);
}

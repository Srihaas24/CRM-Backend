package com.crm.BackendApp.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.crm.BackendApp.entity.Task;

@Repository
public interface TaskRepo extends JpaRepository<Task,Long>, JpaSpecificationExecutor<Task> 
{
	@EntityGraph(attributePaths = {"project", "assignedTo", "assignedBy"})
	Optional<Task> findByIdAndProjectOrganizationId(Long taskId, Long organizationId);
}

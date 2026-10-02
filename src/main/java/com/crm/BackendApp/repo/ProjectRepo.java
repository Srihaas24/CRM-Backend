package com.crm.BackendApp.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crm.BackendApp.entity.Project;

@Repository
public interface ProjectRepo extends JpaRepository<Project,Long>
{
	List<Project> findByOrganizationId(Long id);

	Optional<Project> findByOrganizationIdAndId(Long organization_id, Long project_id );
}

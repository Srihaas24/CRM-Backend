package com.crm.BackendApp.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.enums.Role;

@Repository
public interface UserRepo extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> 
{
	@EntityGraph(attributePaths = {"organization"})
	Optional<User> findByEmail(String email);
	
	boolean existsByEmail(String email);
	
	Optional<User> findByIdAndOrganizationId(Long userId, Long organizationId);

	Optional<User> findFirstByOrganizationIdAndRoleOrderByIdAsc(Long organizationId, Role role);

	Page<User> findAll(Specification<User> specification, Pageable pageable);
}

package com.crm.BackendApp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crm.BackendApp.entity.Organization;

@Repository
public interface OrganizationRepo extends JpaRepository<Organization,Long> 
{

	boolean existsByOrganizationEmail(String organizationEmail);

}

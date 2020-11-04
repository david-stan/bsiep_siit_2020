package com.davidstan.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.davidstan.domain.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
	Organization findByName(String name);
}

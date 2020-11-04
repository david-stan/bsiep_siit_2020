package com.davidstan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.davidstan.domain.Organization;
import com.davidstan.repository.OrganizationRepository;

@Service
public class OrganizationService {
	
	@Autowired
	public OrganizationRepository organizationRepository;
	
	public List<Organization> findAll() {
		return organizationRepository.findAll();
	}
	
	public Organization create(String issuerCN) {
		Organization newOrganization = new Organization();
		newOrganization.setName(issuerCN);
		
		return this.organizationRepository.save(newOrganization);
	}
	
	public void delete(String commonName) {
		Organization organization = this.organizationRepository.findByName(commonName);
		this.organizationRepository.delete(organization);
	}
}

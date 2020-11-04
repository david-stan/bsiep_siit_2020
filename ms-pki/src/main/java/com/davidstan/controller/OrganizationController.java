package com.davidstan.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davidstan.domain.Organization;
import com.davidstan.service.OrganizationService;


@RestController
@RequestMapping(path = "/organizations")
public class OrganizationController {
	
	@Autowired
	public OrganizationService organizationServices;
	
	@GetMapping(path="", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Organization>> getAll()  {
        List<Organization> organizationList = organizationServices.findAll();

        return new ResponseEntity<List<Organization>>(organizationList, HttpStatus.OK);
    }
}

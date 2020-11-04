package com.davidstan.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.davidstan.domain.dto.CreateCertificateDTO;

import feign.Headers;

@FeignClient(name = "ms-pki")
public interface PKIClient {
	
	@PostMapping("/api/siem-certificates/root")
	@Headers("siem-centre-origin: true")
	ResponseEntity<byte[]> createRootCertificate();
	
	@PostMapping("/api/siem-certificates/ca")
    @Headers({"Content-Type: application/json", "siem-centre-origin: true"})
	ResponseEntity<?> createCACertificate(CreateCertificateDTO certificateDTO);
	
	@PostMapping("/api/siem-certificates/signed")
    @Headers({"Content-Type: application/json", "siem-centre-origin: true"})
	ResponseEntity<byte[]> createSignedCertificate(CreateCertificateDTO certificateDTO);
	
	@GetMapping("/api/siem-certificates/export/root")
	@Headers("siem-centre-origin: true")
	ResponseEntity<byte[]> importRootCA();
	
	@GetMapping("/api/siem-certificates/export/root/key")
	@Headers("siem-centre-origin: true")
	ResponseEntity<byte[]> importRootCAKey();
	
	@DeleteMapping("/api/siem-certificates/{commonName}")
	@Headers("siem-centre-origin: true")
	ResponseEntity<?> revokeCertificate(@PathVariable("commonName") String commonName);
	
}

package com.davidstan.controller;

import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davidstan.converter.SubjectDataConverter;
import com.davidstan.model.SubjectData;
import com.davidstan.model.dto.CreateCertificateDTO;
import com.davidstan.model.dto.SubjectDataDTO;
import com.davidstan.service.CertificateService;
import com.davidstan.service.KeyService;
import com.davidstan.service.OrganizationService;

@RestController
@RequestMapping(value = "/certificates")
public class UICertificateController {
	@Autowired
    private KeyService keyService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private SubjectDataConverter subjectDataConverter;
    
    @Autowired
    private OrganizationService organizationService;
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubjectDataDTO>> findAll(Pageable pageable) {
        try {
            List<SubjectData> certList = this.keyService.getAllCertificates();
            List<SubjectDataDTO> certDTOList = this.subjectDataConverter.entityListToDTOList(certList);
            if (certDTOList != null)
                return new ResponseEntity<>(certDTOList, HttpStatus.OK);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        } catch (KeyStoreException | CertificateEncodingException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces =  MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCertificateByAdmin(@Valid @RequestBody CreateCertificateDTO createCertificateDTO) {
    	String subjectCN = createCertificateDTO.getSubjectCN();
        createCertificateDTO.setSubjectCN("CN=" + createCertificateDTO.getSubjectCN());
        createCertificateDTO.setIssuerCN("CN=" + createCertificateDTO.getIssuerCN());
        try {
            X509Certificate cert = this.certificateService.generateCertificateByAdmin(createCertificateDTO.getIssuerCN(),
                    createCertificateDTO.getSubjectCN(), createCertificateDTO.getYearsValid(), createCertificateDTO.isCA());
            if (cert != null) {
            	if (createCertificateDTO.isCA()) {
            		this.organizationService.create(subjectCN);
            	}
                return ResponseEntity.ok(subjectDataConverter.convertX509CertificateToSubjectData(cert));
            }
            else {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping(value="/extendCertificate/{commonName}/{forYears}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> extendCertificate(@PathVariable String commonName,
                                               @PathVariable int forYears) {
        commonName = "CN=" + commonName;
        int newEndYear = Calendar.getInstance().get(Calendar.YEAR) + forYears;
        
        try {

            return ResponseEntity.ok(this.certificateService.extendCertificate(commonName, newEndYear));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    
    @SuppressWarnings("finally")
	@DeleteMapping(value="/{commonName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> revokeCertificate(@PathVariable String commonName) {
        boolean revokeSuccessful = this.certificateService.revokeCertificate("CN=" + commonName);
        if (revokeSuccessful) {
        	try {
            	this.organizationService.delete(commonName);
        	} finally {
        		return ResponseEntity.ok(revokeSuccessful);
        	}
        }
        return new ResponseEntity<>("Certificate revocation unsuccessful", HttpStatus.BAD_REQUEST);
    }
    
    @PostMapping(path = "/root", produces =  MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> createRootCA() {
    	Certificate root = this.certificateService.createRootCertificate();
    	try {
			byte[] stream = root.getEncoded();
			return ResponseEntity.ok().contentLength(stream.length).body(stream);
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
}

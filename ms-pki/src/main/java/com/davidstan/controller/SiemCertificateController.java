package com.davidstan.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davidstan.model.dto.CreateCertificateDTO;
import com.davidstan.service.CertificateService;

@RestController
@RequestMapping(value = "/siem-certificates")
public class SiemCertificateController {
	
	@Autowired
    private CertificateService certificateService;
	
	@PostMapping(path = "/root", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> createRootCA(@RequestHeader("siem-centre-origin") @NotNull String origin) {
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
    
    @PostMapping(
    		path = "/ca",
    		consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createCA(@RequestBody CreateCertificateDTO createCertificateDTO, @RequestHeader("siem-centre-origin") @NotNull String origin) {
        createCertificateDTO.setSubjectCN("CN=" + createCertificateDTO.getSubjectCN());
        createCertificateDTO.setIssuerCN("CN=" + createCertificateDTO.getIssuerCN());
        this.certificateService.createIntermediateCA(createCertificateDTO.getIssuerCN(),
		        createCertificateDTO.getSubjectCN(), createCertificateDTO.getYearsValid());
		
		return new ResponseEntity(HttpStatus.OK);
        
    }
    
    @PostMapping(
    		path = "/signed",
    		consumes = MediaType.APPLICATION_JSON_VALUE,
    		produces =  MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> create(@RequestBody CreateCertificateDTO createCertificateDTO, @RequestHeader("siem-centre-origin") @NotNull String origin) {
        createCertificateDTO.setSubjectCN("cn=" + createCertificateDTO.getSubjectCN());
        createCertificateDTO.setIssuerCN("cn=" + createCertificateDTO.getIssuerCN());
        String keystorePath = this.certificateService.createSignedCertificate(createCertificateDTO.getIssuerCN(),
		        createCertificateDTO.getSubjectCN(), createCertificateDTO.getYearsValid());

        byte[] byteStream;
		try {
			InputStream in = new FileInputStream(keystorePath);
			byteStream = IOUtils.toByteArray(in);
			return ResponseEntity.ok().contentLength(byteStream.length).body(byteStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
        
    }
    

    
    @GetMapping(
    		path = "/export/root",
    		produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> exportRootCA(@RequestHeader("siem-centre-origin") @NotNull String origin) {
    	byte[] byteStream;
        // instead of closing the stream, try-with-resources is used
        try (InputStream in = new FileInputStream("keystore.jks")) {
            KeyStore keyStore = KeyStore.getInstance("JKS", "SUN");
            keyStore.load(in, "p@ssw0rd".toCharArray());

            X509Certificate cert = (X509Certificate) keyStore.getCertificate("cn=root");
            byteStream = cert.getEncoded();
            return ResponseEntity.ok().contentLength(byteStream.length).body(byteStream);
        } catch (IOException | KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    @GetMapping(
    		path = "/export/root/key",
    		produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> exportRootCAKey(@RequestHeader("siem-centre-origin") @NotNull String origin) throws UnrecoverableKeyException {
    	byte[] byteStream;
        // instead of closing the stream, try-with-resources is used
        try (InputStream in = new FileInputStream("keystore.jks")) {
            KeyStore keyStore = KeyStore.getInstance("JKS", "SUN");
            keyStore.load(in, "p@ssw0rd".toCharArray());

            Key key = keyStore.getKey("cn=root", "p@ssw0rd".toCharArray());
            byteStream = ((PrivateKey)key).getEncoded();
            return ResponseEntity.ok().contentLength(byteStream.length).body(byteStream);
        } catch (IOException | KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
    	return null;
    }

    @DeleteMapping(value="/{commonName}")
    public ResponseEntity<?> revokeCertificate(@PathVariable String commonName, @RequestHeader("siem-centre-origin") @NotNull String origin) {
        commonName = "CN=" + commonName;
        boolean revokeSuccessful = this.certificateService.revokeCertificate(commonName);
        if (revokeSuccessful) {
            return ResponseEntity.ok(revokeSuccessful);
        }
        return new ResponseEntity<>("Certificate revocation unsuccessful", HttpStatus.BAD_REQUEST);
    }
    
    
    @PostMapping(path = "/validate")
    public ResponseEntity<Boolean> validate(@RequestBody String commonName) throws KeyStoreException {
    	//Boolean isValid = this.certificateService.isExpired(commonName) || this.certificateService.isRevoked(commonName);
    	if (this.certificateService.isRevoked(commonName)) {
    		return new ResponseEntity<>(true, HttpStatus.OK);
    	}
    	if (this.certificateService.isExpired(commonName)) {
    		return new ResponseEntity<>(true, HttpStatus.OK);
    	}
    	System.out.println("PKI_TEST");
    	return new ResponseEntity<>(false, HttpStatus.OK);
    }

    

}

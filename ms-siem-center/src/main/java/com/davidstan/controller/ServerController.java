package com.davidstan.controller;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.davidstan.domain.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;


import com.davidstan.clients.PKIClient;
import com.davidstan.domain.FilterService;
import com.davidstan.domain.LogObserver;
import com.davidstan.domain.dto.CreateCertificateDTO;
import com.davidstan.domain.dto.LogDTO;
import com.davidstan.socket.SLLServerSocketService;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
public class ServerController implements LogObserver {

	@Autowired
	PKIClient pkiClient;


	private final SimpMessagingTemplate template;

	@Autowired
	public ServerController(SimpMessagingTemplate template) {
		this.template = template;
	}


	@PostMapping("/root")
	public ResponseEntity<?> requestRootCertificate() {
		byte[] root = pkiClient.createRootCertificate().getBody();
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(root);
		
		CertificateFactory certFactory;
		try {
			certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);
		
			System.out.println(cert);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@PostMapping("/cert") 
	public ResponseEntity<?> createCertificate(@RequestBody CreateCertificateDTO dto) {
		byte[] bytes = pkiClient.createSignedCertificate(dto).getBody();

		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
			KeyStore keystore;
			keystore = KeyStore.getInstance("JKS", "SUN");
			keystore.load(inputStream, null);
			keystore.store(new FileOutputStream("server.jks"), "p@ssw0rd".toCharArray());
		} catch (KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException | CertificateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new ResponseEntity(HttpStatus.OK);
		
	}
	
	@PostMapping("/ca") 
	public ResponseEntity<?> createCACertificate(@RequestBody CreateCertificateDTO dto) {
		pkiClient.createCACertificate(dto).getBody();
		
		return new ResponseEntity(HttpStatus.OK);
		
	}
	
	@GetMapping("/import/root")
	public ResponseEntity<?> importRootCA() {
		byte[] bytes = pkiClient.importRootCA().getBody();

		//byte[] bytesKey = pkiClient.importRootCAKey().getBody();
		
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
			CertificateFactory factory = CertificateFactory.getInstance("X509");
			X509Certificate cert = (X509Certificate) factory.generateCertificate(inputStream);

			KeyStore keystore = KeyStore.getInstance("JKS", "SUN");
			keystore.load(null, null);
			keystore.setCertificateEntry("root", cert);
			
			//KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			//PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytesKey));
			
			//keystore.setKeyEntry("root", key, "p@ssw0rd".toCharArray(), new X509Certificate[] {cert});
			keystore.store(new FileOutputStream("trust_root.jks"), "p@ssw0rd".toCharArray());
		} catch (KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException | CertificateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@DeleteMapping(path = "/{commonName}")
	public ResponseEntity<?> revokeCertificate(@PathVariable String commonName) {
		pkiClient.revokeCertificate("client_inv");
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@Autowired
	SLLServerSocketService socketService;
	
	@GetMapping(path = "/init")
	public void initServerSocket() throws Exception {
		socketService.initServer();
	}


	@GetMapping(path = "/getLatestLogs")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<?> getLatestLogs() {
		try {
			return new ResponseEntity<>(this.socketService.getLatestLogs(), HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Autowired
	private FilterService filterService;
	
	@PutMapping(path = "/setRegex")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<String> setRegexValue(@RequestBody String regex) {
		this.filterService.setRegex(regex);
		return new ResponseEntity<>(regex, HttpStatus.OK);
	}

	@GetMapping(path = "/getLatestLogsFilteredByLogType/{logType}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<?> getLatestLogsFilteredByLogType(@PathVariable String logType) {
		try {
			List<LogDTO> filteredLogsByLogType = this.socketService.getLatestLogs();
			if (!logType.equals("ALL")) {
				this.socketService.filter.setFilterByType(logType);
				filteredLogsByLogType = this.socketService.getLatestLogs().stream().
						filter(l -> String.valueOf(l.getType()).equals(logType)).collect(Collectors.toList());
			} else {
				this.socketService.filter.setFilterByType("");
			}
			return new ResponseEntity<>(filteredLogsByLogType, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void logHappened(LogDTO ldto) {
		this.template.convertAndSend("/logs", ldto);
	}

}

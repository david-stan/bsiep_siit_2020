package com.davidstan.service;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.davidstan.exceptions.YearValidWrongValueException;
import com.davidstan.repository.CertificateRepository;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    /*public CertificateService() {
        this.certificateRepository = new CertificateRepository();
        this.generator = new CertificateGenerator();
    }*/

    public X509Certificate createRootCertificate() {
        return this.certificateRepository.createRootCA();
    }
    
    public X509Certificate generateCertificateByAdmin(String issuerCN, String subjectCN, int yearsValid, boolean isCA) {
        return this.certificateRepository.generateCertificateByAdmin(issuerCN, subjectCN, yearsValid, isCA);
    }

    public String createIntermediateCA(String issuerCN, String subjectCN, int yearsValid) {
        return this.certificateRepository.createIntermediateCA(issuerCN, subjectCN, yearsValid);
    }
    
    public String createSignedCertificate(String issuerCN, String subjectCN, int yearsValid) {
        return this.certificateRepository.createSignedCertificate(issuerCN, subjectCN, yearsValid);
    }

    public X509Certificate getCertificateByCN(String cn) {
        return this.certificateRepository.getCertificateByCN(cn);
    }

    public boolean revokeCertificate(String commonName) {
        return this.certificateRepository.revokeCertificate(commonName);
    }

    public boolean isRevoked(String commonName) throws KeyStoreException {
        return this.certificateRepository.isRevoked(commonName);
    }

    public boolean isExpired(String commonName) {
        return this.certificateRepository.isExpired(commonName);
    }

    public boolean extendCertificate(String commonName, int newEndYear) throws KeyStoreException, YearValidWrongValueException {
        if (!this.isRevoked(commonName) && !this.isExpired(commonName)) {

            X509Certificate cert = this.getCertificateByCN(commonName);
            String issuerCN = cert.getIssuerDN().getName();

            ZonedDateTime now = ZonedDateTime.now();

            System.out.println(now.getYear());
            int yearsValid = newEndYear - now.getYear();
            // extending into the past is forbidden! you shall not pass!
            if (yearsValid < 0) {
                throw new YearValidWrongValueException("Wrong year valid range!");
            }

            boolean isCA = cert.getBasicConstraints()!= -1;
            this.createIntermediateCA(issuerCN, commonName, yearsValid);
            return true;
        }
        return false;
    }
    
}

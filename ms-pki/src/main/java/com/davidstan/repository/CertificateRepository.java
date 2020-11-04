package com.davidstan.repository;

import com.davidstan.util.KeyStoreUtil;
import com.davidstan.generator.CertificateGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

@Repository
public class CertificateRepository {

	@Autowired
    private CertificateGenerator certificateGenerator;

    @Autowired
    private KeyStoreUtil keyStoreUtil;

    private String keystoreName = "keystore.jks";
    private char[] keystorePassword = "p@ssw0rd".toCharArray();

    private String revokeKeystoreName = "revoked-certificates-keystore.jks";
    private char[] revokeKeystorePassword = "r3v0k3".toCharArray();

    /*public CertificateRepository() {
        this.certificateGenerator = new CertificateGenerator();
        this.keyStoreUtil = new KeyStoreUtil();
    }*/

    public X509Certificate createRootCA() {
        try {
            return this.certificateGenerator.generateRootCA();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public X509Certificate generateCertificateByAdmin(String issuerCN, String subjectCN, int yearsValid, boolean isCA) {
        try {
            // check if issuer is CA
            if (this.isCA(issuerCN))
                return this.certificateGenerator.generateCertificateByAdmin(issuerCN, subjectCN, yearsValid, isCA);
            else
                return null;
        } catch (IOException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException |
                CertificateException | NoSuchProviderException | InvalidAlgorithmParameterException |
                CertPathBuilderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createIntermediateCA(String issuerCN, String subjectCN, int yearsValid) {
        try {
            // check if issuer is CA
            if (this.isCA(issuerCN))
            	return this.certificateGenerator.generateIntermediateCA(issuerCN, subjectCN, yearsValid);
            else
            	throw new CertificateException("Error");
        } catch (IOException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException |
                CertificateException | NoSuchProviderException | InvalidAlgorithmParameterException |
                CertPathBuilderException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
    public String createSignedCertificate(String issuerCN, String subjectCN, int yearsValid) {
        try {
            // check if issuer is CA
            if (this.isCA(issuerCN))
            	return this.certificateGenerator.generateSignedCertificate(issuerCN, subjectCN, yearsValid);
            else
                throw new CertificateException("Error");
        } catch (IOException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException |
                CertificateException | NoSuchProviderException | InvalidAlgorithmParameterException |
                CertPathBuilderException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    public X509Certificate getCertificateByCN(String cn) {
        keyStoreUtil.loadKeyStore(keystoreName, keystorePassword, this.keyStoreUtil.getKeyStore());
        KeyStore keyStore = keyStoreUtil.getKeyStore();
        X509Certificate cert;
        try {
            cert = (X509Certificate) keyStore.getCertificate(cn);
        } catch (KeyStoreException e) {
            throw new IllegalArgumentException("Certificate does not exist under this alias.");
        }
        if (cert == null) {
            throw new IllegalArgumentException("Certificate does not exist under this alias.");
        }
        return cert;
    }

    public boolean revokeCertificate(String commonName) {
        try {
            KeyStore keyStore = this.keyStoreUtil.getKeyStore();
            this.keyStoreUtil.loadKeyStore(this.keystoreName, this.keystorePassword, keyStore);


            PrivateKey subjectPrivateKey = (PrivateKey) keyStore.getKey(commonName, keystorePassword);
            if (subjectPrivateKey == null) {
                return false;
            }
            java.security.cert.Certificate[] certChain = keyStore.getCertificateChain(commonName);

            keyStore.deleteEntry(commonName);

            this.keyStoreUtil.saveKeyStore(this.keystoreName, this.keystorePassword, keyStore);

            KeyStore revokeKeyStore = this.keyStoreUtil.getRevokedKeyStore();

            this.keyStoreUtil.loadKeyStore(this.revokeKeystoreName, this.revokeKeystorePassword, revokeKeyStore);

            revokeKeyStore.setKeyEntry(commonName, subjectPrivateKey, this.revokeKeystorePassword, certChain);
            revokeKeyStore.store(new FileOutputStream(this.revokeKeystoreName), this.revokeKeystorePassword);

            // it's important that the 'regular' keystore.jks is loaded again
//            this.keyStoreUtil.loadKeyStore(this.keystoreName, this.keystorePassword);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return false;

    }


    public boolean isRevoked(String commonName) throws KeyStoreException {
        keyStoreUtil.loadKeyStore(this.revokeKeystoreName, this.revokeKeystorePassword,
                this.keyStoreUtil.getRevokedKeyStore());
        return this.keyStoreUtil.getRevokedKeyStore().getCertificate(commonName) != null;
    }

    public boolean isCA(String commonName) {
        // if == -1, then it is a CA
        return this.getCertificateByCN(commonName).getBasicConstraints()!= -1;
    }

    public boolean isExpired(String commonName) {
        return this.getCertificateByCN(commonName).getNotAfter().before(new Date());
    }
}

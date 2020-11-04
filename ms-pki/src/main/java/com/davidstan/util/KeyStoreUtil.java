package com.davidstan.util;

import org.springframework.stereotype.Service;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

@Service
public class KeyStoreUtil {

	private KeyStore keyStore;
    private KeyStore revokedKeyStore;

    public KeyStoreUtil() {
        try {
            this.keyStore = KeyStore.getInstance("JKS", "SUN");
            this.revokedKeyStore = KeyStore.getInstance("JKS", "SUN");
        } catch (KeyStoreException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public void loadKeyStore(String fileName, char[] password, KeyStore ks) {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName))) {
            ks.load(in, password);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            try {
                ks.load(null, password);
                saveKeyStore(fileName, password, ks);
            } catch (IOException | CertificateException | NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
        }
    }

    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    public KeyStore getRevokedKeyStore() { return this.revokedKeyStore; }

    public void saveKeyStore(String fileName, char[] password, KeyStore ks) {
        try {
            ks.store(new FileOutputStream(fileName), password);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }
}

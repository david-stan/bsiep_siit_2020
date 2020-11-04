package com.davidstan.repository;


import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateEncodingException;
import java.util.List;

import com.davidstan.model.SubjectData;

public interface KeyRepository {
    List<SubjectData> getAllCertificates() throws KeyStoreException, CertificateEncodingException;

	KeyStore getKeyStore();

}


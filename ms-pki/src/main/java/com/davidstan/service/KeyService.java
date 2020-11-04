package com.davidstan.service;


import java.security.KeyStoreException;
import java.security.cert.CertificateEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.davidstan.model.SubjectData;
import com.davidstan.repository.KeyRepository;

@Service
public class KeyService {
    @Autowired
    private KeyRepository keyRepository;


    public List<SubjectData> getAllCertificates() throws KeyStoreException, CertificateEncodingException {
        return this.keyRepository.getAllCertificates();
    }
}

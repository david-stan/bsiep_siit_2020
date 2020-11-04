package com.davidstan.repository;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Repository;

import com.davidstan.converter.SubjectDataConverter;
import com.davidstan.model.SubjectData;
import com.davidstan.util.KeyStoreUtil;

@Repository
public class KeyRepositoryImpl implements KeyRepository {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private KeyStoreUtil keyStoreUtil = new KeyStoreUtil();

    private final JcaX509CertificateConverter converter =
            new JcaX509CertificateConverter()
                .setProvider(Security.getProvider("BC"));

    private final String subjectDN = "CN=self";

    private String fileName = "keystore.jks";

    //for testing purposes
    private char[] password = "p@ssw0rd".toCharArray();

    private String revokedCertsFileName = "revoked-certificates-keystore.jks";
    private char[] revokedCertsPassword = "r3v0k3".toCharArray();
    
    @Override
    public KeyStore getKeyStore() {
        return this.keyStoreUtil.getKeyStore();
    }

    @Override
    public List<SubjectData> getAllCertificates() throws KeyStoreException, CertificateEncodingException {
        SubjectDataConverter sdc = new SubjectDataConverter();
        List<SubjectData> certList = new ArrayList<>();
        this.keyStoreUtil.loadKeyStore(this.fileName, this.password, this.keyStoreUtil.getKeyStore());
        KeyStore ks = this.getKeyStore();
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate cert = (X509Certificate) ks.getCertificateChain(alias)[0];
            SubjectData sd = sdc.convertX509CertificateToSubjectData(cert);

            certList.add(sd);
        }
        return certList;
    }
}

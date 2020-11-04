package com.davidstan.generator;

import com.davidstan.util.KeyStoreUtil;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.KeyStore.Entry;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CertificateGenerator {

	static {
        Security.addProvider(new BouncyCastleProvider());
    }

    //temporary CA and its private key used to sign new certificates
    private X509Certificate caCertificate;
    private PrivateKey caPrivateKey;

    private final String keystoreName = "keystore.jks";

    //for testing purposes
    private final char[] keystorePassword = "p@ssw0rd".toCharArray();

    @Autowired
    private KeyStoreUtil keyStoreUtil;

    //@Autowired
    //private CertificateRepository certificateRepository;

    private final JcaX509CertificateConverter converter =
            new JcaX509CertificateConverter()
                    .setProvider(Security.getProvider("BC"));


    public X509Certificate generateRootCA() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        keyStoreUtil.loadKeyStore(keystoreName, keystorePassword, this.keyStoreUtil.getKeyStore());
        KeyStore keyStore = this.keyStoreUtil.getKeyStore();

        caCertificate = (X509Certificate) keyStore.getCertificate("Root Certificate");
        if (caCertificate != null) {
            throw new IllegalArgumentException("Root Certificate alredy exists!");
        }

        SecureRandom sr = new SecureRandom();

        PrivateKey rootPrivateKey;
        PublicKey rootPublicKey;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, sr);
        KeyPair keypair = keyGen.generateKeyPair();
        rootPrivateKey = keypair.getPrivate();
        rootPublicKey = keypair.getPublic();

        X500Name x500Name = new X500Name("CN=ROOT");

        ContentSigner contentSigner = signer(rootPrivateKey);
        X509CertificateHolder holder = builder(rootPublicKey, x500Name, x500Name,10, true).
                build(contentSigner);
        X509Certificate rootCertificate = converter.getCertificate(holder);
        X509Certificate[] certificateChain = new X509Certificate[] { rootCertificate };

        keyStore.setKeyEntry("CN=ROOT", rootPrivateKey, keystorePassword, certificateChain);
        //keyStore.setCertificateEntry("Root Certificate", rootCertificate);
        keyStore.store(new FileOutputStream(keystoreName), keystorePassword);

        return rootCertificate;
    }
    

    public X509Certificate generateCertificateByAdmin(String issuerCN, String subjectCN, int yearsValid, boolean isCA)
            throws IOException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, NoSuchProviderException, InvalidAlgorithmParameterException,
            CertPathBuilderException {
    	keyStoreUtil.loadKeyStore(keystoreName, keystorePassword, this.keyStoreUtil.getKeyStore());
        KeyStore keyStore = keyStoreUtil.getKeyStore();

        caPrivateKey = (PrivateKey) keyStore.getKey(issuerCN, keystorePassword);
        if (caPrivateKey == null) {
            throw new RuntimeException("Issuer key does not exist under this alias.");
        }
        caCertificate = (X509Certificate) keyStore.getCertificate(issuerCN);
        if (caCertificate == null) {
            throw new RuntimeException("Issuer CA certificate does not exist under this alias.");
        }
        X509Certificate subCert = (X509Certificate) keyStore.getCertificate(subjectCN);
        if (subCert != null) {
            throw new RuntimeException("Certificate already exists for given subject name.");
        }

        SecureRandom sr = new SecureRandom();

        PrivateKey interPrivateKey;
        PublicKey interPublicKey;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, sr);
        KeyPair keypair = keyGen.generateKeyPair();
        interPrivateKey = keypair.getPrivate();
        interPublicKey = keypair.getPublic();


        ContentSigner contentSigner = signer(caPrivateKey);
        X509CertificateHolder holder = builder(
                interPublicKey, new JcaX509CertificateHolder(caCertificate).getSubject(), new X500Name(subjectCN),
                yearsValid, isCA).build(contentSigner);
        X509Certificate interCertificate = converter.getCertificate(holder);

        //X509Certificate[] certificateChain = new X509Certificate[] { interCertificate, caCertificate };

        X509Certificate rootCert = (X509Certificate) keyStore.getCertificate("CN=root");
        
        CertPath certificateChainPath = verifyIssuerCertificateChain(interCertificate, caCertificate, rootCert);

        List<Certificate> chain = (List) certificateChainPath.getCertificates();
        
        int size = chain.size();
        
        Certificate[] certificateChain = new Certificate[size];

        for (int j = 0; j < size; j++) {
            String certChainCN = ((X509Certificate) chain.get(j)).getSubjectDN().getName();
			/*
			 * if (certificateRepository.isRevoked(certChainCN)) { throw new
			 * IllegalArgumentException("Certificate " + certChainCN +
			 * " in certificate chain is revoked."); }
			 */
            certificateChain[j] = chain.get(j);
        }

        keyStore.setKeyEntry(subjectCN, interPrivateKey, keystorePassword, certificateChain);
        
        //keyStore.setCertificateEntry("Root Certificate", rootCertificate);
        keyStore.store(new FileOutputStream(keystoreName), keystorePassword);
        
        return interCertificate;
    }


    public String generateIntermediateCA(String issuerCN, String subjectCN, int yearsValid)
            throws IOException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, NoSuchProviderException, InvalidAlgorithmParameterException,
            CertPathBuilderException, UnrecoverableEntryException {
        keyStoreUtil.loadKeyStore(keystoreName, keystorePassword, this.keyStoreUtil.getKeyStore());
        KeyStore keyStore = keyStoreUtil.getKeyStore();
        System.out.println(yearsValid);

        caPrivateKey = (PrivateKey) keyStore.getKey(issuerCN, keystorePassword);
        if (caPrivateKey == null) {
            throw new RuntimeException("Issuer key does not exist under this alias.");
        }
        caCertificate = (X509Certificate) keyStore.getCertificate(issuerCN);
        if (caCertificate == null) {
            throw new RuntimeException("Issuer CA certificate does not exist under this alias.");
        }

        SecureRandom sr = new SecureRandom();

        PrivateKey interPrivateKey;
        PublicKey interPublicKey;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, sr);
        KeyPair keypair = keyGen.generateKeyPair();
        interPrivateKey = keypair.getPrivate();
        interPublicKey = keypair.getPublic();


        ContentSigner contentSigner = signer(caPrivateKey);
        X509CertificateHolder holder = builder(
                interPublicKey, new JcaX509CertificateHolder(caCertificate).getSubject(), new X500Name(subjectCN),
                yearsValid, true).build(contentSigner);
        X509Certificate interCertificate = converter.getCertificate(holder);

        //X509Certificate[] certificateChain = new X509Certificate[] { interCertificate, caCertificate };

        X509Certificate rootCert = (X509Certificate) keyStore.getCertificate("CN=root");
        
        CertPath certificateChainPath = verifyIssuerCertificateChain(interCertificate, caCertificate, rootCert);

        List<Certificate> chain = (List) certificateChainPath.getCertificates();
        
        int size = chain.size();
        
        Certificate[] certificateChain = new Certificate[size];

        for (int j = 0; j < size; j++) {
            String certChainCN = ((X509Certificate) chain.get(j)).getSubjectDN().getName();
			/*
			 * if (certificateRepository.isRevoked(certChainCN)) { throw new
			 * IllegalArgumentException("Certificate " + certChainCN +
			 * " in certificate chain is revoked."); }
			 */
            certificateChain[j] = chain.get(j);
        }

        keyStore.setKeyEntry(subjectCN, interPrivateKey, keystorePassword, certificateChain);
        
        //keyStore.setCertificateEntry("Root Certificate", rootCertificate);
        keyStore.store(new FileOutputStream(keystoreName), keystorePassword);
        
        return subjectCN;
    }
    
    public String generateSignedCertificate(String issuerCN, String subjectCN, int yearsValid)
            throws IOException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, NoSuchProviderException, InvalidAlgorithmParameterException,
            CertPathBuilderException, UnrecoverableEntryException {
        keyStoreUtil.loadKeyStore(keystoreName, keystorePassword, this.keyStoreUtil.getKeyStore());
        KeyStore keyStore = keyStoreUtil.getKeyStore();

        caPrivateKey = (PrivateKey) keyStore.getKey(issuerCN, keystorePassword);
        if (caPrivateKey == null) {
            throw new RuntimeException("Issuer key does not exist under this alias.");
        }
        caCertificate = (X509Certificate) keyStore.getCertificate(issuerCN);
        if (caCertificate == null) {
            throw new RuntimeException("Issuer CA certificate does not exist under this alias.");
        }

        SecureRandom sr = new SecureRandom();

        PrivateKey interPrivateKey;
        PublicKey interPublicKey;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, sr);
        KeyPair keypair = keyGen.generateKeyPair();
        interPrivateKey = keypair.getPrivate();
        interPublicKey = keypair.getPublic();


        ContentSigner contentSigner = signer(caPrivateKey);
        X509CertificateHolder holder = builder(
                interPublicKey, new JcaX509CertificateHolder(caCertificate).getSubject(), new X500Name(subjectCN),
                yearsValid, false).build(contentSigner);
        X509Certificate interCertificate = converter.getCertificate(holder);

        //X509Certificate[] certificateChain = new X509Certificate[] { interCertificate, caCertificate };

        X509Certificate rootCert = (X509Certificate) keyStore.getCertificate("CN=root");
        
        CertPath certificateChainPath = verifyIssuerCertificateChain(interCertificate, caCertificate, rootCert);

        List<Certificate> chain = (List) certificateChainPath.getCertificates();
        
        int size = chain.size();
        
        Certificate[] certificateChain = new Certificate[size];

        for (int j = 0; j < size; j++) {
            String certChainCN = ((X509Certificate) chain.get(j)).getSubjectDN().getName();
			/*
			 * if (certificateRepository.isRevoked(certChainCN)) { throw new
			 * IllegalArgumentException("Certificate " + certChainCN +
			 * " in certificate chain is revoked."); }
			 */
            certificateChain[j] = chain.get(j);
        }
        
        String keystorePath = subjectCN.substring(3) + ".jks";
        KeyStore userKeystore = KeyStore.getInstance("JKS", "SUN");
        userKeystore.load(null, null);
        userKeystore.setKeyEntry(subjectCN, interPrivateKey, keystorePassword, certificateChain);
        userKeystore.store(new FileOutputStream(keystorePath), "p@ssw0rd".toCharArray());
        
        keyStore.setKeyEntry(subjectCN, interPrivateKey, keystorePassword, certificateChain);
        keyStore.store(new FileOutputStream("keystore.jks"), "p@ssw0rd".toCharArray());
        
        return keystorePath;
    }

    private ContentSigner signer(PrivateKey privateKey) {
        try {
            return new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(privateKey);
        } catch (OperatorCreationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private X509v3CertificateBuilder builder(PublicKey publicKey, X500Name issuer, X500Name subject, int years,
                                             boolean isCA) {
        try {
            ZonedDateTime now = ZonedDateTime.now();
            BigInteger certSerialNumber = BigInteger.valueOf(now.toEpochSecond());

            Date startDate = Date.from(now.toInstant());
            Date endDate = Date.from(now.plusYears(years).toInstant());
            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                    issuer, certSerialNumber, startDate, endDate, subject, publicKey
            );

            //indicates that it is ok for the certificate to be self-signed
            certificateBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true,
                    new BasicConstraints(isCA));

            return certificateBuilder;
        } catch (CertIOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private CertPath verifyIssuerCertificateChain(X509Certificate newCert, X509Certificate issuerCertificate, X509Certificate rootCertificate) throws KeyStoreException, InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, CertPathBuilderException {
        KeyStore keyStore = keyStoreUtil.getKeyStore();

        String issuerCN = issuerCertificate.getSubjectDN().getName();
        Certificate[] certificates = keyStore.getCertificateChain(issuerCN);
        X509Certificate[] x509Certificates = new X509Certificate[certificates.length];

        for (int i = 0; i < certificates.length; i++) {
            x509Certificates[i] = (X509Certificate) certificates[i];
        }

        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(newCert);

        Set<TrustAnchor> anchorSet = new HashSet<>();
        anchorSet.add(new TrustAnchor(rootCertificate, null));
        Set<X509Certificate> intermediateCASet = new HashSet<>();

        for (X509Certificate cert : x509Certificates) {
            intermediateCASet.add(cert);
        }

        intermediateCASet.add(newCert);

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(anchorSet, selector);

        pkixParams.setRevocationEnabled(false);

        CertStore intermediateCertStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(intermediateCASet), "BC");
        pkixParams.addCertStore(intermediateCertStore);

        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
        PKIXCertPathBuilderResult result =
                (PKIXCertPathBuilderResult) builder.build(pkixParams);

        return result.getCertPath();

    }
}

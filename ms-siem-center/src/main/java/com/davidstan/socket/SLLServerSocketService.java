package com.davidstan.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.davidstan.domain.LogFilter;
import com.davidstan.domain.LogObserver;
import com.davidstan.domain.LogType;
import com.davidstan.domain.dto.LogDTO;
import org.apache.tomcat.jni.Local;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import com.davidstan.domain.LogObserver;
import com.davidstan.domain.dto.LogDTO;

@Service
public class SLLServerSocketService {
	@Autowired
	private LogObserver logObserver;

	@Value("${keystoreLocation}")
	private String keystoreLocation;

	@Value("${keystorePassword}")
	private String keystorePassword;
	
	@Autowired
	public LogFilter filter;

	private static final String[] protocols = new String[] {"TLSv1.2"};
    private static final String[] cipher_suites = new String[] {"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384"};

	private static class ClientSocketWorker extends Thread {
		LogObserver logObserver;
		Logger logger = LoggerFactory.getLogger(ClientSocketWorker.class);
		private LogFilter filter;
		private ArrayList<LogDTO> logs;

		public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
				FileNotFoundException, IOException, KeyManagementException, UnrecoverableKeyException {
			SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
					new SSLContextBuilder().loadTrustMaterial(null, new TrustAllStrategy())
							.loadKeyMaterial(ResourceUtils.getFile("server.jks"), "p@ssw0rd".toCharArray(),
									"p@ssw0rd".toCharArray())
							.build(),
					NoopHostnameVerifier.INSTANCE);

			HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

			ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

			return new RestTemplate(requestFactory);
		}

		private SSLSocket sock;

		public ClientSocketWorker(Socket sock, LogObserver logObserver, ArrayList<LogDTO> logs, LogFilter filter) {
			this.sock = (SSLSocket) sock;
			this.logObserver = logObserver;
			this.logs = logs;
			this.filter = filter;
		}

		public void run() {
			try {
				RestTemplate restTemplate = restTemplate();

				ResponseEntity<Boolean> response = restTemplate.postForEntity(
						"https://localhost:9091/api/siem-certificates/validate", "cn=server", Boolean.class);
				if (response.getBody()) {
					throw new CertificateException("Certificate expired or it is not valid anymore.");
				}

				InputStream is = new BufferedInputStream(sock.getInputStream());
				OutputStream os = new BufferedOutputStream(sock.getOutputStream());
				byte[] data = new byte[2048];
				int len = is.read(data);
				if (len <= 0) {
					throw new IOException("no data received");
				}
				String strData = new String(data, 0, len);

				String[] logArray = strData.split(" ");
//				for (String s : logArray) {
//					System.out.println(s + " ");
//				}
//				System.out.println("KRAJ logArray");
				String timestamp = logArray[0] + " " + logArray[1];

				LogType logType = LogType.valueOf(logArray[2]);
				String logSourceName = logArray[3] + " " + logArray[4];
				String logMessage = String.join(" ", Arrays.copyOfRange(logArray, 5, logArray.length));
				LogDTO log = new LogDTO(timestamp, logMessage, logType, logSourceName);
				switch (logType) {
					case INFO:
						this.logger.info(log.toString());
						break;
					case WARN:
						this.logger.warn(log.toString());
						break;
					case TRACE:
						this.logger.trace(log.toString());
						break;
					case DEBUG:
						this.logger.debug(log.toString());
						break;
					case ERROR:
					case FATAL:
						this.logger.error(log.toString());
						break;
				}
				if (this.filter.getFilterByType().equals("")) {
					this.logObserver.logHappened(log);
				} else {
					if (this.filter.getFilterByType().equals(log.getType().toString())) {
						this.logObserver.logHappened(log);
					}
				}


				System.out.printf("server received %d bytes: %s%n", len, new String(data, 0, len));
				os.write(data, 0, len);
				os.flush();
				sock.close();
				
			} catch (IOException | CertificateException | KeyManagementException | UnrecoverableKeyException
					| KeyStoreException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			
			System.out.println("Worker finished.");
		}

	}

	private SSLServerSocket createSSLServerSocket() throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
		keyManagerFactory.init(getServerKeyStore(), "p@ssw0rd".toCharArray());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(getRootKeyStore());

		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

		SSLServerSocket server = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket(8443);
		server.setEnabledProtocols(protocols);

		// server.setEnabledCipherSuites(cipher_suites);
		return server;
	}

	public void initServer() throws Exception {
		SSLServerSocket server = createSSLServerSocket();

		server.setNeedClientAuth(true);
		ArrayList<LogDTO> logs = new ArrayList<>();
		while (true) {
			new ClientSocketWorker(server.accept(), this.logObserver, logs, this.filter).start();
		}
	}

	private KeyStore getRootKeyStore() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream("root.jks"), "p@ssw0rd".toCharArray());
		return keyStore;
	}

	private KeyStore getServerKeyStore() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream("server.jks"), "p@ssw0rd".toCharArray());
		return keyStore;
	}

	public List<LogDTO> getLatestLogs() throws IOException {
		File f = new File("logfile.log");
		FileInputStream fis = new FileInputStream(f);
		Scanner myReader = new Scanner(f);
		List<LogDTO> toReturn = new ArrayList<>();
		while (myReader.hasNextLine()) {
			String data = myReader.nextLine();
			if (data.contains("LLServerSocketService$ClientSocketWorker")) {
				if (data.matches(filter.getFilterByContent())) {
					toReturn.add(this.convertLineToLog(data));
				}

			}
		}
		if (toReturn.size() >= 10) {
			toReturn = toReturn.subList(toReturn.size() - 11, toReturn.size() - 1);
		}
		return toReturn;
	}

	private LogDTO convertLineToLog(String line) {
		String[] ar = line.split(": ");
		ar = ar[1].split(" ");

		String timestamp = ar[0] + " " + ar[1];
		LogType logType = LogType.valueOf(ar[2].trim());
		String sourceName = ar[3] + " " + ar[4];
		String msg = String.join(" ", Arrays.copyOfRange(ar, 5, ar.length));
		return new LogDTO(timestamp, msg, logType, sourceName);
	}
}

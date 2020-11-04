package com.davidstan.socket;

import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32Util;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.tomcat.jni.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class SSLClientSocketService {

	private static final String root_ca = "root.jks";
	private static final char[] root_pass = "p@ssw0rd".toCharArray();

	private static final String[] protocols = new String[] { "TLSv1.2" };
	private static final String[] cipher_suites = new String[] { "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384" };
	private static final String message = "..message..";

	private static final File CONFIG_FILE = new File(".", "src/main/resources/config.txt");

	public static int batchValue = 3000;

	public static final DateTimeFormatter DATE_TIME_FORMATTER =
			new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss").toFormatter();

	private String timestampOfLastSentOsLog = "";

	public static final String COMPUTER_NAME = Kernel32Util.getComputerName();


	private static KeyStore getClientKeyStore() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream("client.jks"), "p@ssw0rd".toCharArray());
		return keyStore;
	}

	private static KeyStore getRootKeyStore() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(root_ca), root_pass);
		return keyStore;
	}

	private static SSLSocket createSocket(String host, int port) throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(getRootKeyStore());

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
		keyManagerFactory.init(getClientKeyStore(), "p@ssw0rd".toCharArray());

		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

		SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(host, port);

		socket.setEnabledProtocols(protocols);
		// socket.setEnabledCipherSuites(cipher_suites);
		return socket;
	}

	public static RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
			FileNotFoundException, IOException, KeyManagementException, UnrecoverableKeyException {
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder()
				.loadTrustMaterial(null, new TrustAllStrategy()).loadKeyMaterial(ResourceUtils.getFile("client.jks"),
						"p@ssw0rd".toCharArray(), "p@ssw0rd".toCharArray())
				.build(), NoopHostnameVerifier.INSTANCE);

		HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

		return new RestTemplate(requestFactory);
	}

	public void initClient() throws InterruptedException {
		ArrayList<String> logFilePaths = getLogFilePathsFromConfig();
		String regex = getRegexStringFromConfig();
		System.out.println("Sim:" + logFilePaths.get(0));
		String goUp = "..";
		String toAppend = logFilePaths.get(0).substring(logFilePaths.get(0).lastIndexOf(goUp) + goUp.length());
		int count = countSubstringAppearance(logFilePaths.get(0), goUp);
		File simulatorLogFile = CONFIG_FILE;
		while (count > 0) {
			simulatorLogFile = simulatorLogFile.getAbsoluteFile().getParentFile();
			count--;
		}
		simulatorLogFile = new File(simulatorLogFile, toAppend);
		long simulatorLogFileLength = simulatorLogFile.length();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(simulatorLogFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ArrayList<String> logs = new ArrayList<>();
		ArrayList<String> osLogs = new ArrayList<>();
		Advapi32Util.EventLogIterator iter = new Advapi32Util.EventLogIterator("System");
		readOsLogs(osLogs, iter);


		while (true) {
			try (SSLSocket socket = createSocket("localhost", 8443)) {

				socket.startHandshake();
				RestTemplate restTemplate = restTemplate();
				ResponseEntity<Boolean> response = restTemplate.postForEntity(
						"https://localhost:9091/api/siem-certificates/validate", "cn=client", Boolean.class);
				if (response.getBody()) {
					throw new CertificateException("Certificate expired or it is not valid anymore.");
				}

				ArrayList<String> logsFiltered = new ArrayList<>();
				// log stuff
				// if there are unread logs in simulator log file
				if (simulatorLogFileLength < simulatorLogFile.length()) {
					logs.clear();
					logs = readFileLineByLine(simulatorLogFile, br, logs);
					simulatorLogFileLength = simulatorLogFile.length();
					logs.forEach(System.out::println);
					for (String log : logs) {
						if (log.matches(regex)) {
							logsFiltered.add(log);
						}
					}
				}

				while (logsFiltered.size() > 0) {
					// connection with SIEM-center stuff

					InputStream is = new BufferedInputStream(socket.getInputStream());
					OutputStream os = new BufferedOutputStream(socket.getOutputStream());
					os.write(logsFiltered.get(0).getBytes());
					os.flush();
					byte[] data = new byte[2048];
					int len = is.read(data);
					if (len <= 0) {
						throw new IOException("no data received");
					}
					System.out.printf("client received %d bytes: %s%n", len, new String(data, 0, len));

					logsFiltered.remove(0);
				}

				// OS logs stuff
				// reads if there are any new logs
				readOsLogs(osLogs, iter);

				while (osLogs.size() > 0) {
					// connection with SIEM-center stuff

					InputStream is = new BufferedInputStream(socket.getInputStream());
					OutputStream os = new BufferedOutputStream(socket.getOutputStream());
					os.write(osLogs.get(0).getBytes());
					os.flush();

					byte[] data = new byte[2048];
					int len = is.read(data);
					if (len <= 0) {
						throw new IOException("no data received");
					}
					System.out.printf("OS logs client received %d bytes: %s%n",
							len, new String(data, 0, len));
					osLogs.remove(0);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.sleep(batchValue);
		}
	}

	public static ArrayList<String> getLogFilePathsFromConfig() {
		ArrayList<String> logFilePaths = new ArrayList<>();
		ArrayList<String> lines = readFileLineByLine(CONFIG_FILE, null, null);
		for (String line : lines) {
			if (line.contains("files")) {
				String fs = line.split("=")[1];
				String[] filePaths = fs.split(" ");
				logFilePaths.addAll(Arrays.asList(filePaths));
			}
		}
		return logFilePaths;
	}

	public static String getRegexStringFromConfig() {
		String regex = "";
		ArrayList<String> lines = readFileLineByLine(CONFIG_FILE, null, null);
		for (String line : lines) {
			if (line.contains("regex")) {
				regex = line.split("=")[1];
			}
		}
		return regex;
	}

	public static ArrayList<String> readFileLineByLine(File file, BufferedReader bufferedReader,
			ArrayList<String> lines) {
		if (lines == null) {
			lines = new ArrayList<>();
		}

		try {
			if (bufferedReader == null) {
				bufferedReader = new BufferedReader(new FileReader(file));
			}
			for (String line; (line = bufferedReader.readLine()) != null;) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	// counts how many times a substring appears in a string
	public static int countSubstringAppearance(String text, String find) {
		int index = 0, count = 0, length = find.length();
		while ((index = text.indexOf(find, index)) != -1) {
			index += length;
			count++;
		}
		return count;
	}

	public void readOsLogs(ArrayList<String> osLogs, Advapi32Util.EventLogIterator iter) {
		LocalDateTime timeOfLastSentOsLog = LocalDateTime.now().minusHours(5);
		while(iter.hasNext()) {
			Advapi32Util.EventLogRecord record = iter.next();

			LocalDateTime dateTime = LocalDateTime.ofEpochSecond(record.getRecord().TimeGenerated.intValue(), 0, ZoneOffset.UTC);

			if (!this.timestampOfLastSentOsLog.equals("")) {
				timeOfLastSentOsLog = LocalDateTime.parse(this.timestampOfLastSentOsLog, DATE_TIME_FORMATTER);
			}

			// if there are new logs
			if (dateTime.isAfter(timeOfLastSentOsLog)) {

				String logTimestamp = DATE_TIME_FORMATTER.format(dateTime);
				String type = String.valueOf(record.getType());
				String logType = "";
				String logMsg = String.valueOf(record.getInstanceId());
				System.out.println(record.getInstanceId());
				switch (type) {
					case "Informational":
						logType = "INFO";
						break;
					case "Error":
						logType = "ERROR";
						break;
					case "Critical":
						logType = "FATAL";
						break;
					case "Verbose":
						logType = "DEBUG";
						break;
					case "Warning":
						logType = "WARN";
						break;
				}
				String log = logTimestamp + " " + logType + " " + COMPUTER_NAME + "  " + logMsg;
				osLogs.add(log);
				this.timestampOfLastSentOsLog = logTimestamp;
			}
		}
	}
}

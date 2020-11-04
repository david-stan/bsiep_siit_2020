package client;

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class ClientSocketApp {
	private static final String root_ca = "root.jks";
	private static final char[] root_pass = "p@ssw0rd".toCharArray();

	private static final String[] protocols = new String[] {"TLSv1.2"};
    private static final String[] cipher_suites = new String[] {"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384"};
	private static final String message =
            "..message..";

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

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
				TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(getRootKeyStore());

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
		keyManagerFactory.init(getClientKeyStore(), "p@ssw0rd".toCharArray());

		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

		SSLSocket socket = (SSLSocket) sslContext.getSocketFactory()
				.createSocket(host, port);

		socket.setEnabledProtocols(protocols);
		//socket.setEnabledCipherSuites(cipher_suites);
		return socket;
	}


	// counts how many times a substring appears in a string
	public static int countSubstringAppearance(String text, String find) {
		int index = 0, count = 0, length = find.length();
		while( (index = text.indexOf(find, index)) != -1 ) {
			index += length; count++;
		}
		return count;
	}

	public static void main(String[] args) throws InterruptedException {
		ArrayList<String> logFilePaths = getLogFilePathsFromConfig();
		String goUp = "..";
		String toAppend = logFilePaths.get(0).substring(logFilePaths.get(0).lastIndexOf(goUp) + goUp.length());
		int count = countSubstringAppearance(logFilePaths.get(0), goUp);
		File f = new File(".").getAbsoluteFile().getParentFile(); // this is were config.txt is
		while (count > 0) {
			f = f.getAbsoluteFile().getParentFile();
			count--;
		}
		f = new File(f, toAppend);
		long fLength = f.length();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ArrayList<String> logs = new ArrayList<>();

		while(true) {

			try {
				// log stuff
				// if there are unread logs in file
				if (fLength < f.length()) {
					logs.clear();
					logs = readFileLineByLine(f, br, logs);
					fLength = f.length();
					logs.forEach(System.out::println);
				}

				while (logs.size() > 0) {
					// connection with SIEM-center stuff
					SSLSocket socket = createSocket("localhost", 8443);
					InputStream is = new BufferedInputStream(socket.getInputStream());
					OutputStream os = new BufferedOutputStream(socket.getOutputStream());
					os.write(logs.get(0).getBytes());
					os.flush();
					byte[] data = new byte[2048];
					int len = is.read(data);
					if (len <= 0) {
						throw new IOException("no data received");
					}
					System.out.printf("client received %d bytes: %s%n",
							len, new String(data, 0, len));
					logs.remove(0);
				}



	        } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.sleep(10000);
		}
	}

	public static ArrayList<String> getLogFilePathsFromConfig() {
		ArrayList<String> logFilePaths = new ArrayList<>();
		File file = new File("config.txt");
		ArrayList<String> lines = readFileLineByLine(file, null, null);
		for (String line : lines) {
			if (line.contains("files")) {
				String fs = line.split("=")[1];
				String[] filePaths = fs.split(" ");
				logFilePaths.addAll(Arrays.asList(filePaths));
			}
		}
		return logFilePaths;
	}

	public static ArrayList<String> readFileLineByLine(File file, BufferedReader bufferedReader, ArrayList<String> lines) {
		if (lines == null) {
			lines = new ArrayList<>();
		}

		try {
			if (bufferedReader == null) {
				bufferedReader = new BufferedReader(new FileReader(file));
			}
			for(String line; (line = bufferedReader.readLine()) != null; ) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

}

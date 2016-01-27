package sune.ssp.secure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public final class SecurityHelper {
	
	private static final String CERTIFICATE_TYPE = "SunX509";
	private static final String ALGORITHM_NAME	 = "TLS";
	
	public static SSLContext getSSLContext(String password) {
		ByteArrayOutputStream stream = create(password);
		try(ByteArrayInputStream input
				= new ByteArrayInputStream(stream.toByteArray())) {
			return load(input, password);
		} catch(IOException ex) {
		}
		
		return null;
	}
	
	private static ByteArrayOutputStream create(String password) {
		try {
			char[] chars = password.toCharArray();
			KeyStore key = KeyStore.getInstance(KeyStore.getDefaultType());
			key.load(null, chars);
			
			try(ByteArrayOutputStream stream
					= new ByteArrayOutputStream()) {
				key.store(stream, chars);
				return stream;
			}
		} catch(Exception ex) {
		}
		
		return null;
	}
	
	private static SSLContext load(InputStream stream, String password) {
		try {
			char[] chars = password.toCharArray();
			KeyStore key = KeyStore.getInstance(KeyStore.getDefaultType());
			key.load(stream, chars);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(CERTIFICATE_TYPE);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(CERTIFICATE_TYPE);
			kmf.init(key, chars);
			tmf.init(key);
			
			SSLContext context = SSLContext.getInstance(ALGORITHM_NAME); 
			TrustManager[] trustManagers = tmf.getTrustManagers(); 
			context.init(kmf.getKeyManagers(), trustManagers, null);
			return context;
		} catch(Exception ex) {
		} finally {
			try {
				if(stream != null) {
					stream.close();
				}
			} catch(Exception ex) {
			}
		}
		
		return null;
	}
	
	public static SSLServerSocket createServer(int port, String password) {
		try {
			SSLServerSocket socket =
				(SSLServerSocket) getSSLContext(password)
				.getServerSocketFactory()
				.createServerSocket(port);
			socket.setEnabledProtocols(
				socket.getSupportedProtocols());
			socket.setEnabledCipherSuites(
				socket.getSupportedCipherSuites());
			return socket;
		} catch(Exception ex) {
		}
		
		return null;
	}
	
	public static SSLSocket createClient(String serverIP, int serverPort) {
		try {
			SSLSocket socket =
				(SSLSocket) SSLContext.getDefault()
				.getSocketFactory()
				.createSocket(serverIP, serverPort);
			socket.setEnabledProtocols(
				socket.getSupportedProtocols());
			socket.setEnabledCipherSuites(
				socket.getSupportedCipherSuites());
			return socket;
		} catch(Exception ex) {
		}
		
		return null;
	}
}
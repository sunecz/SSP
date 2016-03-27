package sune.ssp.secure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public final class SecurityManager {
	
	public static final class SecurityData {
		
		public final String certificate;
		public final String protocol;
		public final Provider provider;
		
		public SecurityData(String certificate, String protocol,
				Provider provider) {
			this.certificate = certificate;
			this.protocol 	 = protocol;
			this.provider	 = provider;
		}
	}
	
	private static SecurityData securityData;
	static {
		// Test whether the TLS protocol can be used. If the TLS protocol
		// cannot be used, the default protocol is used.
		String contextProtocol = "TLS";
		try {
			SSLContext.getInstance(contextProtocol);
		} catch(NoSuchAlgorithmException ex) {
			try {
				contextProtocol
					= SSLContext.getDefault()
								.getProtocol();
			} catch(Exception ex0) {
				throw new IllegalStateException(
					"Cannot get default SSLContext protocol!");
			}
		}
		securityData = new SecurityData(
			KeyManagerFactory.getDefaultAlgorithm(),
			contextProtocol, null);
	}
	
	public static void setSecurityData(SecurityData data) {
		securityData = data;
	}
	
	public static SecurityData getSecurityData() {
		return securityData;
	}
	
	protected static SSLContext getSSLContext(String password) {
		try(ByteArrayInputStream input
				= new ByteArrayInputStream(
					create(password).toByteArray())) {
			return load(input, password);
		} catch(Exception ex) {
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
			
			KeyManagerFactory kmf =
				securityData.provider == null ?
					KeyManagerFactory.getInstance(
						securityData.certificate) :
					KeyManagerFactory.getInstance(
						securityData.certificate,
						securityData.provider);
			TrustManagerFactory tmf =
				securityData.provider == null ? 
					TrustManagerFactory.getInstance(
						securityData.certificate) :
					TrustManagerFactory.getInstance(
						securityData.certificate,
						securityData.provider);
			kmf.init(key, chars);
			tmf.init(key);
			
			SSLContext context =
				securityData.provider == null ?
					SSLContext.getInstance(
						securityData.protocol) :
					SSLContext.getInstance(
						securityData.protocol,
						securityData.provider);
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
	
	protected static SSLServerSocket createServer(int port, String password) {
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
	
	protected static SSLSocket createClient(String serverIP, int serverPort) {
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
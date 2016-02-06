package sune.ssp.secure;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import sune.ssp.Server;
import sune.ssp.ServerClient;
import sune.ssp.crypt.CryptedData;
import sune.ssp.crypt.Session;
import sune.ssp.crypt.SimpleSession;
import sune.ssp.crypt.SymmetricKey;
import sune.ssp.data.Data;

public class SecureServer extends Server {
	
	private final String password;
	private final Session session;
	private final HashMap<String, PublicKey> publicKeys;
	private final HashMap<String, SymmetricKey> symmetricKeys;
	
	protected SecureServer(String ipAddress, int port, String password) {
		super(ipAddress, port);
		this.password	   = password;
		this.session	   = SimpleSession.createSession();
		this.publicKeys    = new LinkedHashMap<>();
		this.symmetricKeys = new LinkedHashMap<>();
	}
	
	public static SecureServer create(int port, String password) {
		try {
			return new SecureServer(
				InetAddress.getLocalHost().getHostAddress(), port, password);
		} catch(Exception ex) {
		}
		
		return null;
	}
	
	@Override
	protected ServerSocket createSocket(int port) throws IOException {
		SSLServerSocket socket = SecurityHelper.createServer(port, password);
		socket.setSoTimeout(8000);
		return socket;
	}
	
	@Override
	protected ServerClient createClient(Socket socket) {
		return new SecureServerClient(this, (SSLSocket) socket);
	}
	
	@Override
	protected void addDataToSend(Data data, String senderIP) {
		SymmetricKey key;
		if((key = symmetricKeys.get(senderIP)) != null) {
			// Encrypt the data using client's symmetric key
			data = new CryptedData(key, data);
		}
		super.addDataToSend(data, senderIP);
	}
	
	public void addPublicKey(String ipAddress, PublicKey key) {
		synchronized(publicKeys) {
			publicKeys.put(ipAddress, key);
		}
	}
	
	public PublicKey getPublicKey(String ipAddress) {
		synchronized(publicKeys) {
			return publicKeys.get(ipAddress);
		}
	}
	
	public void addSymmetricKey(String ipAddress, SymmetricKey key) {
		synchronized(symmetricKeys) {
			symmetricKeys.put(ipAddress, key);
		}
	}
	
	public SymmetricKey getSymmetricKey(String ipAddress) {
		synchronized(symmetricKeys) {
			return symmetricKeys.get(ipAddress);
		}
	}
	
	public PublicKey getPublicKey() {
		return session.getPublicKey();
	}
	
	protected Session getSession() {
		return session;
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}
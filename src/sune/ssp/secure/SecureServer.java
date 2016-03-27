package sune.ssp.secure;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import sune.ssp.Server;
import sune.ssp.ServerClient;
import sune.ssp.util.PortUtils;
import sune.util.crypt.Crypt;
import sune.util.crypt.CryptMethod;

public class SecureServer extends Server {
	
	private final String password;
	private final Session session;
	private final HashMap<String, PublicKey> publicKeys;
	private final HashMap<String, SymmetricKey> symmetricKeys;
	private final CryptMethod cryptMethod;
	
	protected SecureServer(String ipAddress, int port, String password,
			CryptMethod cryptMethod, Session session) {
		super(ipAddress, port);
		this.password	   = password;
		this.session	   = session;
		this.publicKeys    = new LinkedHashMap<>();
		this.symmetricKeys = new LinkedHashMap<>();
		this.cryptMethod   = cryptMethod;
	}
	
	public static SecureServer create(int port, String password) {
		return create(port, password, Crypt.getAES(), SimpleSession.createSession());
	}
	
	public static SecureServer create(int port, String password,
			CryptMethod cryptMethod, Session session) {
		return new SecureServer(PortUtils.getLocalIpAddress(), port, password,
				cryptMethod, session);
	}
	
	@Override
	protected ServerSocket createSocket(int port) throws IOException {
		SSLServerSocket socket = SecurityManager.createServer(port, password);
		socket.setSoTimeout(TIMEOUT);
		return socket;
	}
	
	@Override
	protected ServerClient createClient(Socket socket) {
		return new SecureServerClient(this, (SSLSocket) socket);
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
	
	public CryptMethod getCryptMethod() {
		return cryptMethod;
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}
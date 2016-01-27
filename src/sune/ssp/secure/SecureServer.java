package sune.ssp.secure;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import sune.ssp.Server;
import sune.ssp.ServerClient;

public class SecureServer extends Server {
	
	private final String password;
	
	protected SecureServer(String ipAddress, int port, String password) {
		super(ipAddress, port);
		this.password = password;
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
	public boolean isSecure() {
		return true;
	}
}
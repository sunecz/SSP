package sune.ssp.secure;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;

import sune.ssp.Client;

public class SecureClient extends Client {
	
	protected SecureClient(String serverIP, int serverPort) {
		super(serverIP, serverPort);
	}
	
	public static SecureClient create(String serverIP, int serverPort) {
		return new SecureClient(serverIP, serverPort);
	}
	
	@Override
	protected Socket createSocket(String serverIP, int serverPort)
			throws UnknownHostException, IOException {
		SSLSocket socket = SecurityHelper.createClient(serverIP, serverPort);
		socket.setSoTimeout(8000);
		socket.startHandshake();
		return socket;
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}
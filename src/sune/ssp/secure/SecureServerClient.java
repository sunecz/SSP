package sune.ssp.secure;

import javax.net.ssl.SSLSocket;

import sune.ssp.ServerClient;

public class SecureServerClient extends ServerClient {
	
	protected SecureServerClient(SecureServer server, SSLSocket socket) {
		super(server, socket);
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}
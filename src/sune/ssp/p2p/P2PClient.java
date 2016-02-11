package sune.ssp.p2p;

import sune.ssp.secure.SecureServer;
import sune.ssp.util.PortUtils;

public class P2PClient {
	
	private final SecureServer server;
	private final MultiClient multiClient;
	
	protected P2PClient(int serverPort, String password) {
		this.server  	 = SecureServer.create(serverPort, password);
		this.multiClient = new MultiClient();
		this.server.setServerName(PortUtils.getLocalIpAddress());
		this.server.setForceSend(true);
	}
	
	public static P2PClient create(int serverPort, String password) {
		return new P2PClient(serverPort, password);
	}
	
	public void connectTo(String serverIP, int serverPort) {
		multiClient.append(serverIP, serverPort);
	}
	
	public void start() {
		if(!multiClient.isEmpty()) {
			server.start();
			multiClient.connectAll();
		}
	}
	
	public void stop() {
		multiClient.disconnectAll();
		server.stop();
	}
	
	public SecureServer getServer() {
		return server;
	}
	
	public MultiClient getClient() {
		return multiClient;
	}
}
package sune.ssp.fast;

import sune.ssp.util.PortUtils;

public class FastTest {
	
	public static void main(String[] args) {
		FastServer server = new FastServer(8888);
		server.start();
		
		FastClient client = new FastClient(PortUtils.getIpAddress(), 9999);
		client.connect();
	}
}
package gui;

import sune.ssp.data.Message;
import sune.ssp.event.ClientEvent;
import sune.ssp.event.ServerEvent;
import sune.ssp.p2p.P2PClient;
import sune.ssp.util.PortUtils;
import sune.ssp.util.Utils;

public class P2PTest {
	
	private static final int PORT0 = 8500;
	private static final int PORT1 = 8501;
	private static final int PORT2 = 8502;
	
	public static void main(String[] args) {
		String ipAddress  = PortUtils.getLocalIpAddress();
		P2PClient client0 = P2PClient.create(PORT0, "simplepassword0");
		P2PClient client1 = P2PClient.create(PORT1, "simplepassword1");
		P2PClient client2 = P2PClient.create(PORT2, "simplepassword2");
		
		client0.connectTo(ipAddress, PORT1);
		client0.connectTo(ipAddress, PORT2);
		client0.getClient().addListener(ClientEvent.CONNECTED, (value) -> {
			println("Connected0");
			//client.send("Hello there!");
		});
		client0.getServer().addListener(ServerEvent.DATA_RECEIVED, (data) -> {
			if(data instanceof Message) {
				Message message = (Message) data;
				System.out.printf(
					"Client0 received a message from %s: %s\n", 
					message.getSenderIP(), message.getMessage());
			}
		});
		client0.getServer().addListener(ServerEvent.STARTED, (data) -> {
			println("Started0");
		});
		
		client1.connectTo(ipAddress, PORT0);
		client1.connectTo(ipAddress, PORT2);
		client1.getClient().addListener(ClientEvent.CONNECTED, (value) -> {
			println("Connected1");
			//client.send("Hello there!");
		});
		client1.getServer().addListener(ServerEvent.STARTED, (data) -> {
			println("Started1");
		});
		
		client2.connectTo(ipAddress, PORT0);
		client2.connectTo(ipAddress, PORT1);
		client2.getClient().addListener(ClientEvent.CONNECTED, (value) -> {
			println("Connected2");
			//client.send("Hello there from number 2!");
		});
		client2.getServer().addListener(ServerEvent.STARTED, (data) -> {
			println("Started2");
		});
		
		new Thread(() -> client0.start()).start();
		new Thread(() -> client1.start()).start();
		new Thread(() -> client2.start()).start();
		new Thread(() -> {
			Utils.sleep(1000);
			client2.getClient().send("Hello there:3");
		}).start();
	}
	
	private static final synchronized void println(String text) {
		System.out.println(text);
	}
}
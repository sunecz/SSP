package sune.ssp.fast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import sune.ssp.etc.IPAddress;
import sune.test.matrix.Utils;

public class FastServer {
	
	private IPAddress ipAddress;
	
	private DatagramSocket socket;
	private DatagramPacket spacket;
	private DatagramPacket rpacket;
	
	private static final int BUFFER_SIZE = 8192;
	private byte[] sbuffer = new byte[BUFFER_SIZE];
	private byte[] rbuffer = new byte[BUFFER_SIZE];
	
	private Queue<byte[]> dataToSend;
	private Queue<byte[]> dataReceived;
	private List<IPAddress> clients;
	
	private volatile boolean running;
	
	private Thread threadSend;
	private Runnable runSend = (() -> {
		byte[] data;
		while(running) {
			try {
				synchronized(dataToSend) {
					data = dataToSend.poll();
				}
				if(data != null) {
					int len = data.length;
					int off = 0;
					do {
						spacket.setData(data, off,
							len > BUFFER_SIZE ?
								  BUFFER_SIZE : len);
						socket.send(spacket);
						off += BUFFER_SIZE;
						len -= BUFFER_SIZE;
					} while(len > 0);
				}
			} catch(Exception ex) {
			}
			Utils.sleep(1);
		}
	});
	
	private Thread threadReceive;
	private Runnable runReceive = (() -> {
		while(running) {
			try {
				synchronized(rpacket) {
					socket.receive(rpacket);
					String addr = rpacket.getAddress().getHostAddress();
					int    port = rpacket.getPort();
					synchronized(clients) {
						if(!hasClient(addr, port)) {
							addClient(addr, port);
						}
					}
					synchronized(dataReceived) {
						dataReceived.add(
							Arrays.copyOf(rbuffer, rbuffer.length));
					}
				}
			} catch(Exception ex) {
			}
			Utils.sleep(1);
		}
	});
	
	public FastServer(int port) {
		this.ipAddress    = IPAddress.getLocal(port);
		this.dataToSend   = new ConcurrentLinkedQueue<>();
		this.dataReceived = new ConcurrentLinkedQueue<>();
	}
	
	public void start() {
		try {
			socket = new DatagramSocket(ipAddress.getPort());
			socket.setSoTimeout(8000);
			
			spacket = new DatagramPacket(sbuffer, BUFFER_SIZE);
			rpacket = new DatagramPacket(rbuffer, BUFFER_SIZE);
			
			running 	  = true;
			threadSend	  = new Thread(runSend);
			threadReceive = new Thread(runReceive);
			threadSend.start();
			threadReceive.start();
		} catch(Exception ex) {
		}
	}
	
	public void send(byte[] data) {
		dataToSend.add(data);
	}
	
	void addClient(String addr, int port) {
		clients.add(new IPAddress(addr, port));
		System.out.println("Client added!");
	}
	
	boolean hasClient(String addr, int port) {
		for(IPAddress client : clients) {
			if(client.getPort() == port &&
			   client.getIP().equals(addr)) {
				return true;
			}
		}
		return false;
	}
}
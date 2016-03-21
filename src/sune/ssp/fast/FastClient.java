package sune.ssp.fast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import sune.ssp.etc.Connection;
import sune.ssp.etc.IPAddress;
import sune.test.matrix.Utils;

public class FastClient {
	
	private Connection connection;
	
	private DatagramSocket socket;
	private DatagramPacket spacket;
	private DatagramPacket rpacket;
	
	private static final int BUFFER_SIZE = 8192;
	private byte[] sbuffer = new byte[BUFFER_SIZE];
	private byte[] rbuffer = new byte[BUFFER_SIZE];
	
	private Queue<byte[]> dataToSend;
	private Queue<byte[]> dataReceived;
	
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
	
	public FastClient(String ipAddress, int port) {
		this.connection   = new Connection(
			IPAddress.getLocal(),
			new IPAddress(ipAddress, port));
		this.dataToSend   = new ConcurrentLinkedQueue<>();
		this.dataReceived = new ConcurrentLinkedQueue<>();
	}
	
	public void connect() {
		try {
			socket = new DatagramSocket(
				new InetSocketAddress(
					connection.getDestination().getIP(),
					connection.getDestination().getPort()));
			socket.setSoTimeout(8000);
			
			spacket = new DatagramPacket(sbuffer, BUFFER_SIZE);
			rpacket = new DatagramPacket(rbuffer, BUFFER_SIZE);
			
			connection = new Connection(
				new IPAddress(socket.getLocalAddress()
									.getHostAddress(),
							  socket.getPort()),
				connection.getDestination());
			
			running 	  = true;
			threadSend	  = new Thread(runSend);
			threadReceive = new Thread(runReceive);
			threadSend.start();
			threadReceive.start();
			
			send(new byte[] {7, 7, 6, 6, 5, 5});
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void send(byte[] data) {
		dataToSend.add(data);
	}
}
package sune.ssp.util;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PortUtils {
	
	public static String getIpAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static String getIpAddress(String url) {
		try {
			return InetAddress.getByName(
				new URL(url).getHost()).getHostAddress();
		} catch(Exception ex) {
			System.err.printf(
				"Cannot get IP address of %s!\n", url);
			return null;
		}
	}
	
	public static boolean isPortOpen(String ipAddress, int port) {
		return isPortOpen(ipAddress, port, 8000);
	}
	
	public static boolean isPortOpen(String ipAddress, int port, int timeout) {
		InetSocketAddress addr = new InetSocketAddress(ipAddress, port);
		try(Socket socket = new Socket()) {
			socket.connect(addr, timeout);
			return true;
		} catch(Exception ex0) {
			try(DatagramSocket socket = new DatagramSocket()) {
				socket.setSoTimeout(timeout);
				socket.connect(addr);
				return true;
			} catch(Exception ex1) {
				return false;
			}
		}
	}
	
	public static int[] getOpenPorts(String ipAddress, boolean fast) {
		List<Integer> ports = new ArrayList<>();
		if(fast) {
			if(isPortOpen(ipAddress, 21))  ports.add(21);
			if(isPortOpen(ipAddress, 22))  ports.add(22);
			if(isPortOpen(ipAddress, 80))  ports.add(80);
			if(isPortOpen(ipAddress, 443)) ports.add(443);
		} else {
			for(int i = 0; i <= 65535; ++i) {
				if(isPortOpen(ipAddress, i))
					ports.add(i);
			}
		}
		
		return Utils.toIntArray(
			ports.toArray(new Integer[ports.size()]));
	}
	
	public static int getRandomOpenPort(String ipAddress) {
		int port 	= -1;
		Random rand = new Random();
		do {
			port = rand.nextInt();
		} while(!isPortOpen(ipAddress, port));
		return port;
	}
}
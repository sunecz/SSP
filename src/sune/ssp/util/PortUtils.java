package sune.ssp.util;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

public class PortUtils {
	
	private static final int MAX_PORT = 1 << 16;
	
	public static String getIpAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch(Exception ex) {
		}
		return null;
	}
	
	// Code taken from:
	// http://stackoverflow.com/questions/17252018/getting-my-lan-ip-address-192-168-xxxx-ipv4
	// The code has been modified!
	public static String getLocalIpAddress() {
		try {
			Enumeration<NetworkInterface> interfaces
				= NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()) {
				NetworkInterface ni 			   = interfaces.nextElement();
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while(addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if(addr instanceof Inet4Address &&
					  !addr.isLoopbackAddress()		&&
					  // The link-local ip address is in range 169.254.x.x.
					  // That is the address that we do not need and we should
					  // prevent it from being meant as the local ip address.
					  !addr.isLinkLocalAddress()) {
						return addr.getHostAddress().toString();
					}
				}
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static String getIpAddress(String url) {
		if(url == null || url.isEmpty()) {
			throw new IllegalArgumentException(
				"URL cannot be null or empty!");
		}
		try {
			return InetAddress.getByName(
				new URL(url).getHost()).getHostAddress();
		} catch(Exception ex) {
			throw new IllegalStateException(
				"Cannot get IP address of " + url + "!\n");
		}
	}
	
	public static boolean isPortOpen(String ipAddress, int port) {
		return isPortOpen(ipAddress, port, 8000);
	}
	
	public static boolean isPortOpen(String ipAddress, int port, int timeout) {
		if(ipAddress == null || ipAddress.isEmpty()) {
			throw new IllegalArgumentException(
				"IP address cannot be null or empty!");
		}
		if(port < 0 || port >= MAX_PORT) {
			throw new IllegalArgumentException(
				"Incorrect port value, should be in range " +
				"0 - " + MAX_PORT + "!");
		}
		if(timeout < 0) {
			throw new IllegalArgumentException(
				"Timeout has to be positive!");
		}
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
		if(ipAddress == null || ipAddress.isEmpty()) {
			throw new IllegalArgumentException(
				"IP address cannot be null or empty!");
		}
		List<Integer> ports = new ArrayList<>();
		if(fast) {
			if(isPortOpen(ipAddress, 21))  ports.add(21);
			if(isPortOpen(ipAddress, 22))  ports.add(22);
			if(isPortOpen(ipAddress, 80))  ports.add(80);
			if(isPortOpen(ipAddress, 443)) ports.add(443);
		} else {
			for(int i = 0; i < MAX_PORT; ++i) {
				if(isPortOpen(ipAddress, i))
					ports.add(i);
			}
		}
		
		return Utils.toIntArray(
			ports.toArray(new Integer[ports.size()]));
	}
	
	public static int getRandomOpenPort(String ipAddress) {
		if(ipAddress == null || ipAddress.isEmpty()) {
			throw new IllegalArgumentException(
				"IP address cannot be null or empty!");
		}
		int port 	= -1;
		Random rand = new Random();
		do {
			port = rand.nextInt();
		} while(!isPortOpen(ipAddress, port));
		return port;
	}
}
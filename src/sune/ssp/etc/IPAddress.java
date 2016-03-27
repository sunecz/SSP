package sune.ssp.etc;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.regex.Pattern;

import sune.ssp.util.PortUtils;

public class IPAddress {
	
	private static final String REGEX_IPV4
		= "^(2[0-4][0-9]|25[0-5]|[0-1]\\d{0,2}|\\d{1,2})\\." +
		  "(2[0-4][0-9]|25[0-5]|[0-1]\\d{0,2}|\\d{1,2})\\."  +
		  "(2[0-4][0-9]|25[0-5]|[0-1]\\d{0,2}|\\d{1,2})\\."  +
		  "(2[0-4][0-9]|25[0-5]|[0-1]\\d{0,2}|\\d{1,2})$";
	
	private final String ipAddress;
	private final InetAddress addr;
	private final boolean ipv6;
	private final int port;
	
	public IPAddress(String ipAddress) {
		this(ipAddress, -1);
	}
	
	public IPAddress(String ipAddress, int port) {
		if(ipAddress == null || ipAddress.isEmpty()) {
			throw new IllegalArgumentException(
				"IP Address cannot be null nor empty!");
		}
		if(!isValidPort(port)) {
			throw new IllegalArgumentException(
				"Port is out of range (0-65535)!");
		}
		this.ipAddress = ipAddress;
		this.addr 	   = getInetAddress(ipAddress);
		this.ipv6	   = addr instanceof Inet6Address;
		this.port	   = port;
	}
	
	static InetAddress getInetAddress(String ipAddress) {
		try {
			byte[] raw = null;
			if(Pattern.matches(REGEX_IPV4, ipAddress)) {
				// Resolve for IPv4 format
				String[] parts = ipAddress.split("\\.");
				raw    = new byte[4];
				raw[0] = (byte) Integer.parseInt(parts[0]);
				raw[1] = (byte) Integer.parseInt(parts[1]);
				raw[2] = (byte) Integer.parseInt(parts[2]);
				raw[3] = (byte) Integer.parseInt(parts[3]);
			} else {
				// Resolve for IPv6 format
				String[] parts = ipAddress.split("\\:");
				raw 		   = new byte[16];
				for(int i = 0, k = 0; i < 8; ++i, k+=2) {
					String part = parts[i];
					short value = Short.parseShort(part, 16);
					raw[k] 	    = (byte) ((value)	   & 0xffff);
					raw[k+1]    = (byte) ((value >> 8) & 0xffff);
				}
			}
			if(raw != null) {
				return InetAddress.getByAddress(raw);
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	static String IPv4ToIPv6(String ipAddress) {
		String[] parts = ipAddress.split("\\.");
		byte[] ipv6    = new byte[16];
		byte[] ipv4    = new byte[4];
		for(int i = 0; i < 4; ++i) {
			ipv4[i] = (byte) Integer.parseInt(parts[i]);
		}
		ipv6[10] = (byte) 0xff;
		ipv6[11] = (byte) 0xff;
		ipv6[12] = ipv4[0];
		ipv6[13] = ipv4[1];
		ipv6[14] = ipv4[2];
		ipv6[15] = ipv4[3];
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 16; i+=2) {
			int val = ((ipv6[i]    & 0xff) << 8) |
					  ((ipv6[i+1]) & 0xff);
			if(i   != 0) sb.append(":");
			if(val != 0) sb.append(
				Integer.toHexString(val));
		}
		return sb.toString();
	}
	
	public Connection createConnection(String dest) {
		return createConnection(dest, 0);
	}
	
	public Connection createConnection(String dest, int port) {
		return createConnection(new IPAddress(dest));
	}
	
	public Connection createConnection(IPAddress dest) {
		return new Connection(this, dest);
	}
	
	public boolean isValid() {
		return addr != null;
	}
	
	public boolean isIPv4() {
		return isValid() && !ipv6;
	}
	
	public boolean isIPv6() {
		return isValid() && ipv6;
	}
	
	public InetAddress getInetAddress() {
		return addr;
	}
	
	public String getIP() {
		return !isValid() ? null : ipAddress;
	}
	
	public String getIPv6() {
		return ipv6 ? getIP() : IPv4ToIPv6(ipAddress);
	}
	
	public int getPort() {
		return port;
	}
	
	public static boolean isValidPort(int port) {
		return port >= 0 && port < PortUtils.MAX_PORT;
	}
	
	public static boolean isValidCustomPort(int port) {
		return port > 1024 && isValidPort(port);
	}
	
	public static boolean isValidIPv4(String ipAddress) {
		if(ipAddress == null || ipAddress.isEmpty())
			return false;
		String[] parts = ipAddress.split("\\.");
		if(parts.length != 4) return false;
		for(String part : parts) {
			try {
				int val = Integer.parseInt(part);
				if(val < 0 || val > 255) {
					return false;
				}
			} catch(Exception ex) {
				return false;
			}
		}
		return true;
	}
	
	public static IPAddress getLocal() {
		return getLocal(0);
	}
	
	public static IPAddress getLocal(int port) {
		return new IPAddress(PortUtils.getLocalIpAddress(), port);
	}
}
package gui;
public class ClientTableInfo {
	
	private final String ipAddress;
	private final String uuid;
	private final String username;
	
	public ClientTableInfo(String ipAddress, String uuid, String username) {
		this.ipAddress = ipAddress;
		this.uuid	   = uuid;
		this.username  = username;
	}
	
	public void setIP(String ipAddress) {}
	public void setUUID(String uuid) {}
	public void setUsername(String username) {}
	
	public String getIP() {
		return ipAddress;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public String getUsername() {
		return username;
	}
}
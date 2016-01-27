package gui;
public class ClientTableInfo {
	
	private String ipAddress;
	private String username;
	
	public ClientTableInfo(String ipAddress, String username) {
		this.ipAddress = ipAddress;
		this.username  = username;
	}
	
	public void setIP(String ipAddress) {}
	public void setUsername(String username) {}
	
	public String getIP() {
		return ipAddress;
	}
	
	public String getUsername() {
		return username;
	}
}
package sune.ssp.etc;

import sune.ssp.data.Data;

public class ServerClientInfo extends Data {
	
	private static final long serialVersionUID = 2611084921759964621L;

	public ServerClientInfo(String ipAddress, String username) {
		super("ipAddress", ipAddress, "username", username);
	}
	
	public String getIP() {
		return (String) getData("ipAddress");
	}
	
	public String getUsername() {
		return (String) getData("username");
	}
}
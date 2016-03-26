package sune.ssp.etc;

import sune.ssp.data.Data;

public class ServerClientInfo extends Data {
	
	private static final long serialVersionUID = 2611084921759964621L;

	public ServerClientInfo(String ipAddress, String uuid, String username) {
		super("ipAddress", ipAddress,
			  "uuid",	   uuid,
			  "username",  username);
	}
	
	public String getIP() {
		return (String) getData("ipAddress");
	}
	
	public String getUUID() {
		return (String) getData("uuid");
	}
	
	public String getUsername() {
		return (String) getData("username");
	}
}
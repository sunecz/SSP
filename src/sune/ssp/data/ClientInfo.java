package sune.ssp.data;

public class ClientInfo extends Data {
	
	private static final long serialVersionUID = -3239163980794326640L;

	public ClientInfo(String username) {
		super("username", username);
	}
	
	public String getUsername() {
		return (String) getData("username");
	}
}
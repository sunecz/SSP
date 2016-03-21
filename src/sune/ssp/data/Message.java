package sune.ssp.data;

public class Message extends Data {
	
	private static final long serialVersionUID = 8126931444429972531L;
	
	public Message(String message, String username) {
		super("message",  message,
			  "username", username);
	}
	
	public String getMessage() {
		return (String) getData("message");
	}
	
	public String getUsername() {
		return (String) getData("username");
	}
}
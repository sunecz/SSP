package sune.ssp.data;

public class Message extends Data {
	
	private static final long serialVersionUID = 8126931444429972531L;
	
	public Message(String message, String username) {
		super("message",  message,
			  "username", username);
	}
	
	public String getMessage() {
		return new Value(getData("message")).stringValue();
	}
	
	public String getUsername() {
		return new Value(getData("username")).stringValue();
	}
}
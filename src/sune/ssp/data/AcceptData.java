package sune.ssp.data;

public class AcceptData extends Data {
	
	private static final long serialVersionUID = -4323089873704674105L;
	
	public AcceptData(String hash) {
		this(hash, false);
	}
	
	public AcceptData(String hash, boolean waitState) {
		super("hash", hash,
			  "wait", waitState);
	}
	
	public String getHash() {
		return (String) getData("hash");
	}
	
	public boolean isWaitState() {
		return (boolean) getData("wait");
	}
}
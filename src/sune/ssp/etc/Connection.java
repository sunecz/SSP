package sune.ssp.etc;

public class Connection {
	
	private final IPAddress source;
	private final IPAddress dest;
	
	public Connection(String source, String dest) {
		if(source == null || source.isEmpty()) {
			throw new IllegalArgumentException(
				"Source address cannot be null nor empty!");
		}
		if(dest == null || dest.isEmpty()) {
			throw new IllegalArgumentException(
				"Destination address cannot be null nor empty!");
		}
		this.source = new IPAddress(source);
		this.dest	= new IPAddress(dest);
	}
	
	public Connection(IPAddress source, IPAddress dest) {
		if(source == null) {
			throw new IllegalArgumentException(
				"Source address cannot be null!");
		}
		if(dest == null) {
			throw new IllegalArgumentException(
				"Destination address cannot be null!");
		}
		this.source = source;
		this.dest 	= dest;
	}
	
	public IPAddress getSource() {
		return source;
	}
	
	public IPAddress getDestination() {
		return dest;
	}
}
package sune.ssp.etc;

public class Connection {
	
	private final IPAddress source;
	private final IPAddress dest;
	
	public Connection(String source, String dest) {
		this(new IPAddress(source), new IPAddress(dest));
	}
	
	public Connection(IPAddress source, IPAddress dest) {
		if(source == null || dest == null) {
			throw new IllegalArgumentException(
				"Source and destination address have to be non-null!");
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
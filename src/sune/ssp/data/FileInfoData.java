package sune.ssp.data;

public class FileInfoData extends Data {
	
	private static final long serialVersionUID = -4394357725268332435L;
	
	public FileInfoData(String hash, String name, long size, long waitTime) {
		super("hash", 	  hash,
			  "name", 	  name,
			  "size", 	  size,
			  "waitTime", waitTime);
	}
	
	public String getHash() {
		return new Value(getData("hash")).stringValue();
	}
	
	public String getName() {
		return new Value(getData("name")).stringValue();
	}
	
	public long getSize() {
		return new Value(getData("size")).longValue();
	}
	
	public long getWaitTime() {
		return new Value(getData("waitTime")).longValue();
	}
}
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
		return (String) getData("hash");
	}
	
	public String getName() {
		return (String) getData("name");
	}
	
	public long getSize() {
		return (long) getData("size");
	}
	
	public long getWaitTime() {
		return (long) getData("waitTime");
	}
}
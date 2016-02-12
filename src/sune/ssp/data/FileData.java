package sune.ssp.data;

public class FileData extends Data {
	
	private static final long serialVersionUID = 60706002599889145L;
	
	public FileData(String hash, byte[] data, long total) {
		this(hash, data, total, data != null ? data.length : -1);
	}
	
	public FileData(String hash, byte[] data, long total, int length) {
		super("hash",   hash,
			  "data",   data,
			  "total",  total,
			  "length", length);
	}
	
	public String getHash() {
		return (String) getData("hash");
	}
	
	public byte[] getRawData() {
		return (byte[]) getData("data");
	}
	
	public int getLength() {
		return (int) getData("length");
	}
	
	public long getTotalSize() {
		return (long) getData("total");
	}
}
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
		return new Value(getData("hash")).stringValue();
	}
	
	public byte[] getRawData() {
		return new Value(getData("data")).value(byte[].class);
	}
	
	public int getLength() {
		return new Value(getData("length")).intValue();
	}
	
	public long getTotalSize() {
		return new Value(getData("total")).longValue();
	}
}
package sune.util.compression;

public class Compression {
	
	public static final ZLibCompression getZLib() {
		return new ZLibCompression();
	}
	
	public static final GZipCompression getGZip() {
		return new GZipCompression();
	}
}
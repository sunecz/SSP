package sune.util.compression;

import java.io.InputStream;
import java.io.OutputStream;

public interface CompressionMethod {
	
	public void compress(InputStream istream, OutputStream ostream);
	public void decompress(InputStream istream, OutputStream ostream);
}
package sune.util.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class ZLibCompression implements CompressionMethod {
	
	private final int bufferSize;
	
	public ZLibCompression() {
		this(8192);
	}
	
	public ZLibCompression(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	@Override
	public final void compress(InputStream istream, OutputStream ostream) {
		try(DeflaterOutputStream dstream
				= new DeflaterOutputStream(ostream,
						new Deflater(Deflater.BEST_COMPRESSION,
									 true), bufferSize)) {
			CompressionUtils.transferBytes(
				istream, dstream, bufferSize);
			dstream.finish();
			istream.close();
		} catch(Exception ex) {
		}
	}
	
	@Override
	public final void decompress(InputStream istream, OutputStream ostream) {
		try(InflaterInputStream fstream
				= new InflaterInputStream(istream,
						new Inflater(true), bufferSize)) {
			CompressionUtils.transferBytes(
				fstream, ostream, bufferSize);
			ostream.close();
		} catch(Exception ex) {
		}
	}
}
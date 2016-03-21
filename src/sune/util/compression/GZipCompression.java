package sune.util.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipCompression implements CompressionMethod {
	
	private final int bufferSize;
	
	public GZipCompression() {
		this(8192);
	}
	
	public GZipCompression(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	@Override
	public final void compress(InputStream istream, OutputStream ostream) {
		try(GZIPOutputStream gstream
				= new GZIPOutputStream(ostream, bufferSize)) {
			CompressionUtils.transferBytes(
				istream, gstream, bufferSize);
			gstream.finish();
			istream.close();
		} catch(Exception ex) {
		}
	}
	
	@Override
	public final void decompress(InputStream istream, OutputStream ostream) {
		try(GZIPInputStream gstream
				= new GZIPInputStream(istream, bufferSize)) {
			CompressionUtils.transferBytes(
				gstream, ostream, bufferSize);
			ostream.close();
		} catch(Exception ex) {
		}
	}
}
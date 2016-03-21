package sune.util.compression;

import java.io.InputStream;
import java.io.OutputStream;

public class CompressionUtils {
	
	public static final void transferBytes(InputStream  istream,
			OutputStream ostream, int bufferSize) {
		try {
			int    read	  = 0;
			byte[] buffer = new byte[bufferSize];
			while((read = istream.read(buffer)) != -1) {
				ostream.write(buffer, 0, read);
				ostream.flush();
			}
		} catch(Exception ex) {
		}
	}
}
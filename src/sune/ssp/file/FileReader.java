package sune.ssp.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileReader {
	
	private static final int MAX_MARK 	 = Integer.MAX_VALUE - 8;
	private static final int BUFFER_SIZE = 8192;
	
	private final File file;
	private final byte[] buffer;
	private InputStream reader;
	private boolean initialized;
	private int current;
	private long total;
	
	protected FileReader(File file, int bufferSize, long totalSize) {
		if(file == null) {
			throw new IllegalArgumentException(
				"File object cannot be null!");
		}
		if(bufferSize <= 0) {
			throw new IllegalArgumentException(
				"Buffer size must be non-zero and positive!");
		}
		this.file 	 = file;
		this.buffer  = new byte[bufferSize];
		this.current = 0;
		this.total 	 = totalSize;
	}
	
	public static FileReader create(File file, long totalSize) {
		return new FileReader(file, BUFFER_SIZE, totalSize);
	}
	
	public boolean initialize() {
		if(initialized) return true;
		if(reader == null) {
			try {
				reader = new BufferedInputStream(
					new FileInputStream(file));
				reader.mark(MAX_MARK);
				return initialized = true;
			} catch(Exception ex) {
			}
		}
		return false;
	}
	
	public byte[] read() {
		if(initialize()) {
			try {
				int read = reader.read(buffer);
				if(read <= 0) return null;
				byte[] data = new byte[read];
				System.arraycopy(buffer, 0, data, 0, read);
				if((current) != -1 &&
				   (current  += read) >= total) {
					close();
				}
				return data;
			} catch(Exception ex) {
			}
		}
		return null;
	}
	
	public boolean reset() {
		if(initialize()) {
			try {
				reader.reset();
				return true;
			} catch(Exception e) {
			}
		}
		return false;
	}
	
	public boolean close() {
		if(initialize()) {
			try {
				reader.close();
				initialized = false;
				return true;
			} catch(Exception ex) {
				close();
			} finally {
				if(!initialized) {
					reader = null;
				}
			}
		}
		return false;
	}
	
	public boolean isRead() {
		return current >= total;
	}
	
	public int getCurrent() {
		return current;
	}
	
	public long getTotal() {
		return total;
	}
	
	public File getFile() {
		return file;
	}
}
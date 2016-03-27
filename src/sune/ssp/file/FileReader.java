package sune.ssp.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileReader {
	
	private final File file;
	private InputStream reader;
	private int current;
	private long total;
	
	private boolean initialized;
	private boolean closed;
	
	protected FileReader(File file, long totalSize) {
		if(file == null) {
			throw new IllegalArgumentException(
				"File object cannot be null!");
		}
		if(totalSize < 0) {
			throw new IllegalArgumentException(
				"Invalid file's total size! Has to be >= 0.");
		}
		this.file 	 = file;
		this.current = 0;
		this.total 	 = totalSize;
	}
	
	public boolean initialize() {
		if(initialized) return true;
		if(closed)		return false;
		if(reader == null) {
			try {
				reader = new BufferedInputStream(
					new FileInputStream(file));
				return initialized = true;
			} catch(Exception ex) {
			}
		}
		return false;
	}
	
	public int read(byte[] buffer) {
		if(initialize()) {
			try {
				int read = reader.read(buffer);
				if(read == -1) return -1;
				if((current) != -1 &&
				   (current  += read) >= total) {
					close();
				}
				return read;
			} catch(Exception ex) {
			}
		}
		return -1;
	}
	
	public boolean close() {
		if(initialize()) {
			try {
				if(reader != null) {
					reader.close();
					closed = true;
				}
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
	
	public boolean isClosed() {
		return closed;
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
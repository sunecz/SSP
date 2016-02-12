package sune.ssp.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FileWriter {
	
	private final File file;
	private OutputStream writer;
	private boolean initialized;
	private int current;
	private long total;
	
	protected FileWriter(File file, long totalSize) {
		if(file == null) {
			throw new IllegalArgumentException(
				"File object cannot be null!");
		}
		this.file 	 = file;
		this.current = 0;
		this.total	 = totalSize;
	}
	
	public static FileWriter create(File file, long totalSize) {
		return new FileWriter(file, totalSize);
	}
	
	public boolean initialize() {
		if(initialized) return true;
		if(writer == null) {
			try {
				writer = new BufferedOutputStream(
					new FileOutputStream(file));
				return initialized = true;
			} catch(Exception ex) {
			}
		}
		return false;
	}
	
	public boolean write(byte[] data) {
		if(initialize()) {
			try {
				writer.write(data);
				writer.flush();
				if((current) != -1 &&
				   (current  += data.length) >= total) {
					close();
				}
				return true;
			} catch(Exception ex) {
			}
		}
		return false;
	}
	
	public boolean close() {
		if(initialize()) {
			try {
				writer.close();
				initialized = false;
				return true;
			} catch(Exception ex) {
				close();
			} finally {
				if(!initialized) {
					writer = null;
				}
			}
		}
		return false;
	}
	
	public boolean isWritten() {
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
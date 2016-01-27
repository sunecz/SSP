package sune.ssp.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileSaver {
	
	private BufferedOutputStream stream;
	
	protected FileSaver(File file) throws FileNotFoundException {
		this.stream = new BufferedOutputStream(
			new FileOutputStream(file));
	}
	
	public static FileSaver create(File file) {
		try {
			return new FileSaver(file);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public boolean save(byte[] data) {
		try {
			stream.write(data);
			stream.flush();
			return true;
		} catch(Exception ex) {
		}
		return false;
	}
	
	public void close() {
		try {
			stream.close();
		} catch(Exception ex) {
			if(stream != null) {
				close();
			}
		}
	}
}
package sune.ssp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javafx.scene.image.Image;

public class Resource {
	
	public static URL resource(String path) {
		if(path == null || path.isEmpty()) {
			throw new IllegalArgumentException(
				"Path cannot be null or empty!");
		}
		return Resource.class.getResource(path);
	}
	
	public static InputStream stream(String path) {
		if(path == null || path.isEmpty()) {
			throw new IllegalArgumentException(
				"Path cannot be null or empty!");
		}
		return Resource.class.getResourceAsStream(path);
	}
	
	public static Image image(String path) {
		return new Image(stream(path));
	}
	
	public static File file(String path) {
		return file(path, "temp_", ".tmp");
	}
	
	public static File file(String path, String prefix, String suffix) {
		if(path == null || path.isEmpty()) {
			throw new IllegalArgumentException(
				"Path cannot be null or empty!");
		}
		try {
			File file = File.createTempFile(prefix, suffix);
			file.deleteOnExit();
			try(InputStream is = stream(path)) {
				try(FileOutputStream os = new FileOutputStream(file)) {
					byte[] buffer = new byte[8192];
					
					int read;
					while((read = is.read(buffer)) != -1)
						os.write(buffer, 0, read);
					return file;
				}
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static String stylesheet(String path) {
		return resource(path).toExternalForm();
	}
}
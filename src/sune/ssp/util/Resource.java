package sune.ssp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javafx.scene.image.Image;

public class Resource {
	
	public static URL resource(String path) {
		return Resource.class.getResource(path);
	}
	
	public static InputStream stream(String path) {
		return Resource.class.getResourceAsStream(path);
	}
	
	public static Image image(String path) {
		return new Image(stream(path));
	}
	
	public static File file(String path) {
		return file("temp_", path);
	}
	
	public static File file(String prefix, String path) {
		try {
			File file = File.createTempFile(prefix, ".tmp");
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
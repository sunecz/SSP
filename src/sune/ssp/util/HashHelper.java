package sune.ssp.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Formatter;

public class HashHelper {
	
	public static final String sha1(File file) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
			try(InputStream stream = new BufferedInputStream(
					new FileInputStream(file))) {
				byte[] buffer = new byte[8192];
				
				int read = 0;
				while((read = stream.read(buffer)) != -1) {
					messageDigest.update(buffer, 0, read);
				}
			}
			
			try(Formatter formatter = new Formatter()) {
				for(byte b : messageDigest.digest()) {
					formatter.format("%02x", b);
				}
				
				return formatter.toString();
			}
		} catch(Exception ex) {
		}
		return null;
	}
}
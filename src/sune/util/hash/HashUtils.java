package sune.util.hash;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public final class HashUtils {
	
	private static final String  NAME_SHA1;
	private static final String  NAME_SHA256;
	private static final String  NAME_MD5;
	
	static {
		NAME_SHA1	= "SHA-1";
		NAME_SHA256 = "SHA-256";
		NAME_MD5 	= "MD5";
	}
	
	private static final byte[] hash(byte[] bytes, String name) {
		try {
			return MessageDigest
				.getInstance(name)
				.digest(bytes);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final byte[] sha1(byte[] bytes) {
		return hash(bytes, NAME_SHA1);
	}
	
	public static final byte[] sha256(byte[] bytes) {
		return hash(bytes, NAME_SHA256);
	}
	
	public static final byte[] md5(byte[] bytes) {
		return hash(bytes, NAME_MD5);
	}
	
	public static final byte[] toNbit(byte[] bytes, int bits) {
		if(bits != 128 && bits != 256) {
			throw new UnsupportedOperationException(
				"Number of bits has to equal to 128 or 256!");
		}
		return bits == 128 ? md5(bytes)    :
			   bits == 256 ? sha256(bytes) :
			   null;
	}
	
	public static final byte[] hashf(File file, String name) {
		try {
			MessageDigest digest   = MessageDigest.getInstance(name);
			try(InputStream stream = new BufferedInputStream(
					new FileInputStream(file))) {
				int read 	  = 0;
				byte[] buffer = new byte[8192];
				while((read = stream.read(buffer)) != -1)
					digest.update(buffer, 0, read);
			}
			return digest.digest();
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final byte[] sha1f(File file) {
		return hashf(file, NAME_SHA1);
	}
	
	public static final byte[] sha256f(File file) {
		return hashf(file, NAME_SHA256);
	}
	
	public static final byte[] md5f(File file) {
		return hashf(file, NAME_MD5);
	}
	
	public static final String toHexString(String string) {
		return toHexString(string.getBytes());
	}
	
	public static final String toHexString(byte[] bytes) {
	    return new BigInteger(1, bytes).toString(16);
	}
}
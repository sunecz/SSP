package sune.ssp.crypt;

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
}
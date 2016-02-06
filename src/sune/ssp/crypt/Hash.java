package sune.ssp.crypt;

import java.math.BigInteger;
import java.nio.charset.Charset;

public final class Hash {
	
	static final Charset CHARSET = Charset.forName("UTF-8");
	static final byte[] toUTF8Bytes(String string) {
		return string.getBytes(CHARSET);
	}
	
	public static final String sha1(String string) {
		return new String(HashUtils.sha1(toUTF8Bytes(string)));
	}
	
	public static final String sha256(String string) {
		return new String(HashUtils.sha256(toUTF8Bytes(string)));
	}
	
	public static final String md5(String string) {
		return new String(HashUtils.md5(toUTF8Bytes(string)));
	}
	
	public static final String to128bit(String string) {
		return toNbit(string, 128);
	}
	
	public static final String to256bit(String string) {
		return toNbit(string, 256);
	}
	
	public static final String toNbit(String string, int bits) {
		return toHexString(HashUtils.toNbit(toUTF8Bytes(string), bits));
	}
	
	public static final String toHexString(String string) {
		return toHexString(string.getBytes());
	}
	
	public static final String toHexString(byte[] bytes) {
	    return new BigInteger(1, bytes).toString(16);
	}
}
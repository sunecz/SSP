package sune.util.hash;

import java.io.File;
import java.nio.charset.Charset;

public final class Hash {
	
	static final Charset CHARSET = Charset.forName("UTF-8");
	static final byte[] toUTF8Bytes(String string) {
		return string.getBytes(CHARSET);
	}
	
	public static final HashSHA1 getSHA1() {
		return new HashSHA1();
	}
	
	public static final HashSHA256 getSHA256() {
		return new HashSHA256();
	}
	
	public static final HashMD5 getMD5() {
		return new HashMD5();
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
	
	public static final String sha1f(File file) {
		return HashUtils.toHexString(HashUtils.sha1f(file));
	}
	
	public static final String sha256f(File file) {
		return HashUtils.toHexString(HashUtils.sha256f(file));
	}
	
	public static final String md5f(File file) {
		return HashUtils.toHexString(HashUtils.md5f(file));
	}
	
	public static final String to128bit(String string) {
		return toNbit(string, 128);
	}
	
	public static final String to256bit(String string) {
		return toNbit(string, 256);
	}
	
	public static final String toNbit(String string, int bits) {
		return HashUtils.toHexString(HashUtils.toNbit(toUTF8Bytes(string), bits));
	}
}
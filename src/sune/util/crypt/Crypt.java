package sune.util.crypt;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class Crypt {
	
	static final Charset CHARSET = Charset.forName("UTF-8");
	static final String encodeUTF8(String string) {
		return new String(string.getBytes(), CHARSET);
	}
	
	public static final CryptAES getAES() {
		return new CryptAES();
	}
	
	public static final String encrypt(String string, PublicKey key) {
		return CryptRSA.encrypt(encodeUTF8(string), key);
	}
	
	public static final String decrypt(String string, PrivateKey key) {
		return CryptRSA.decrypt(encodeUTF8(string), key);
	}
	
	public static final KeyPair weakRSAKeyPair() {
		return CryptRSA.generateKeyPair(2048);
	}
	
	public static final KeyPair strongRSAKeyPair() {
		return CryptRSA.generateKeyPair(4096);
	}
	
	private static final class HackCrypt {
		
		// Code taken from:
		// http://suhothayan.blogspot.cz/2012/05/how-to-install-java-cryptography.html
		static final Field fieldRestrict;
		static {
			Field field = null;
			try {
				field = Class.forName("javax.crypto.JceSecurity")
							 .getDeclaredField("isRestricted");
				field.setAccessible(true);
			} catch(Exception ex) {
			}
			fieldRestrict = field;
		}
		
		public static void set(boolean value)
				throws IllegalArgumentException,
					   IllegalAccessException {
			fieldRestrict.setBoolean(null, value);
		}
		
		public static boolean get()
				throws IllegalArgumentException,
					   IllegalAccessException {
			return fieldRestrict.getBoolean(null);
		}
	}
	
	public static final boolean unlimitedKeySize() {
		try {
			return !HackCrypt.get();
		} catch(Exception ex) {
		}
		return false;
	}
	
	public static final boolean unlimitedKeySize(boolean value) {
		try {
			HackCrypt.set(!value);
			return true;
		} catch(Exception ex) {
		}
		return false;
	}
}
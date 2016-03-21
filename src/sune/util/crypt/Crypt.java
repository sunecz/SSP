package sune.util.crypt;

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
	
	public static final String encodeBase64(String string) {
		return Base64.encodeString(encodeUTF8(string));
	}
	
	public static final String decodeBase64(String string) {
		return Base64.decodeString(encodeUTF8(string));
	}
	
	public static final String encodeBase64URL(String string) {
		return Base64URL.encodeString(encodeUTF8(string));
	}
	
	public static final String decodeBase64URL(String string) {
		return Base64URL.decodeString(encodeUTF8(string));
	}
	
	public static final String encrypt(String string, PublicKey key) {
		return CryptKey.encrypt(encodeUTF8(string), key);
	}
	
	public static final String decrypt(String string, PrivateKey key) {
		return CryptKey.decrypt(encodeUTF8(string), key);
	}
	
	public static final KeyPair generateRSAKeyPair() {
		return CryptKey.generateKeyPair();
	}
}
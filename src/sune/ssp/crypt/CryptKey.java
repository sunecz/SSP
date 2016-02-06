package sune.ssp.crypt;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

import sune.ssp.util.Randomizer;

public final class CryptKey {
	
	private static final String  ALGORITHM_CIPHER;
	private static final Charset CHARSET;
	
	static {
		ALGORITHM_CIPHER = "RSA";
		CHARSET			 = Crypt.CHARSET;
	}
	
	private static final boolean checkAction(int action) {
		return action == Cipher.ENCRYPT_MODE ||
			   action == Cipher.DECRYPT_MODE;
	}
	
	private static final byte[] doAction(byte[] string, Key key, int action) {
		if(string == null || string.length == 0) {
			throw new IllegalArgumentException(
				"Text that should be encrypted or decrypted " +
				"cannot be null or empty!");
		}
		if(key == null) {
			throw new IllegalArgumentException(
				"Key cannot be null!");
		}
		if(!checkAction(action)) {
			throw new UnsupportedOperationException(
				"Action " + action + " is not supported!");
		}
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER);
			cipher.init(action, key);
			return cipher.doFinal(string);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String encrypt(String string, Key key) {
		try {
			return Base64.encode(
				doAction(
					string.getBytes(CHARSET),
					key, Cipher.ENCRYPT_MODE));
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String decrypt(String string, Key key) {
		try {
			return new String(
				doAction(
					Base64.decode(string),
					key, Cipher.DECRYPT_MODE),
				CHARSET);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String encrypt(String string, PublicKey key) {
		return encrypt(string, (Key) key);
	}
	
	public static final String decrypt(String string, PrivateKey key) {
		return decrypt(string, (Key) key);
	}
	
	public static final KeyPair generateKeyPair() {
		try {
			KeyPairGenerator generator
				= KeyPairGenerator.getInstance(ALGORITHM_CIPHER);
			generator.initialize(2048,
				Randomizer.createSecureStrong());
			return generator.generateKeyPair();
		} catch(Exception ex) {
		}
		return null;
	}
}
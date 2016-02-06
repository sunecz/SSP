package sune.ssp.crypt;

import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import sune.ssp.util.Randomizer;

public final class CryptAES {
	
	private static final String  ALGORITHM_CIPHER;
	private static final String  ALGORITHM_KEY;
	private static final Charset CHARSET;
	
	static {
		ALGORITHM_CIPHER = "AES/ECB/PKCS5Padding";
		ALGORITHM_KEY	 = "AES";
		CHARSET			 = Crypt.CHARSET;
	}
	
	private static final boolean checkAction(int action) {
		return action == Cipher.ENCRYPT_MODE ||
			   action == Cipher.DECRYPT_MODE;
	}
	
	private static final byte[] doAction(byte[] string, byte[] key, int action) {
		if(string == null || string.length == 0) {
			throw new IllegalArgumentException(
				"Text that should be encrypted or decrypted " +
				"cannot be null or empty!");
		}
		if(key == null || key.length != 16) {
			throw new IllegalArgumentException(
				"Encryption and decryption supports only " +
				"128-bit key (16 characters).");
		}
		if(!checkAction(action)) {
			throw new UnsupportedOperationException(
				"Action " + action + " is not supported!");
		}
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER);
			cipher.init(action, new SecretKeySpec(key, ALGORITHM_KEY));
			return cipher.doFinal(string);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String encrypt(String string, String key) {
		try {
			return Base64.encode(
				doAction(
					string.getBytes(CHARSET),
					key.getBytes(CHARSET),
					Cipher.ENCRYPT_MODE));
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String decrypt(String string, String key) {
		try {
			return new String(
				doAction(
					Base64.decode(string),
					key.getBytes(CHARSET),
					Cipher.DECRYPT_MODE),
				CHARSET);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String generateKey() {
		try {
			KeyGenerator generator
				= KeyGenerator.getInstance(ALGORITHM_KEY);
			generator.init(128, Randomizer.createSecure());
			return Hash.toHexString(
				HashUtils.toNbit(
					generator.generateKey()
							 .getEncoded(),
					128)).substring(0, 16);
		} catch(Exception ex) {
		}
		return null;
	}
}
package sune.util.crypt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sune.ssp.util.Randomizer;
import sune.util.encode.Base64;
import sune.util.hash.HashUtils;

public final class CryptAES implements CryptMethod {
	
	private static final String  ALGORITHM_CIPHER;
	private static final String  ALGORITHM_KEY;
	private static final Charset CHARSET;
	
	static {
		ALGORITHM_CIPHER = "AES/ECB/PKCS5Padding";
		ALGORITHM_KEY	 = "AES";
		CHARSET			 = Crypt.CHARSET;
	}
	
	private static final Cipher getCipher() {
		try {
			return Cipher.getInstance(ALGORITHM_CIPHER);
		} catch(Exception ex) {
		}
		return null;
	}
	
	private static final SecretKey getSecretKey(byte[] key) {
		return new SecretKeySpec(key, ALGORITHM_KEY);
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
		if(!checkAction(action)) {
			throw new UnsupportedOperationException(
				"Action " + action + " is not supported!");
		}
		try {
			Cipher cipher = getCipher();
			cipher.init(action, getSecretKey(key));
			return cipher.doFinal(string);
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public final byte[] encrypt(byte[] bytes, byte[] key) {
		try {
			return doAction(
				bytes, key,
				Cipher.ENCRYPT_MODE);
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public final String encrypt(String string, String key) {
		try {
			return Base64.encodeString(
				encrypt(
					string.getBytes(CHARSET),
					key.getBytes(CHARSET)));
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public final byte[] decrypt(byte[] bytes, byte[] key) {
		try {
			return doAction(
				bytes, key,
				Cipher.DECRYPT_MODE);
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public final String decrypt(String string, String key) {
		try {
			return new String(
				decrypt(
					Base64.decode0(string),
					key.getBytes(CHARSET)),
				CHARSET);
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public final byte[] keyBytes(String key) {
		return key.getBytes(CHARSET);
	}
	
	@Override
	public String generateKey(int bits) {
		try {
			KeyGenerator generator
				= KeyGenerator.getInstance(ALGORITHM_KEY);
			generator.init(bits, Randomizer.createSecure());
			return HashUtils.toHexString(
				HashUtils.toNbit(
					generator.generateKey()
							 .getEncoded(),
					bits)).substring(0, bits / 8);
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public String weakKey() {
		return generateKey(128);
	}
	
	@Override
	public String strongKey() {
		return generateKey(256);
	}
	
	/* ------------------------------------------
	 * |										|
	 * |		  Stream cryptography			|
	 * |										|
	 * ------------------------------------------*/
	
	private static final int BUFFER_ENCRYPT_SIZE = 8192;
	private static final int BUFFER_DECRYPT_SIZE = 8208;
	
	private static final void streamcrypt(InputStream istream, OutputStream ostream,
			int bufferSize, byte[] key, int action) {
		if(istream == null || ostream == null) {
			throw new IllegalArgumentException(
				"Input/Output stream cannot be null!");
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
			Cipher cipher = getCipher();
			cipher.init(action, getSecretKey(key));
			try(CipherInputStream cstream
					= new CipherInputStream(istream, cipher)) {
				int read	  = 0;
				byte[] buffer = new byte[bufferSize];
				while((read = cstream.read(buffer)) != -1) {
					ostream.write(buffer, 0, read);
					ostream.flush();
				}
				ostream.close();
			}
		} catch(Exception ex) {
		}
	}
	
	@Override
	public final void encrypt(InputStream istream, OutputStream ostream, byte[] key) {
		streamcrypt(istream, ostream, BUFFER_ENCRYPT_SIZE, key, Cipher.ENCRYPT_MODE);
	}
	
	@Override
	public final void decrypt(InputStream istream, OutputStream ostream, byte[] key) {
		streamcrypt(istream, ostream, BUFFER_DECRYPT_SIZE, key, Cipher.DECRYPT_MODE);
	}
	
	private static final long datacrypt(InputStream istream, DataOutput ostream,
			int bufferSize, byte[] key, int action) {
		if(istream == null || ostream == null) {
			throw new IllegalArgumentException(
				"Input/Output stream cannot be null!");
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
			Cipher cipher = getCipher();
			cipher.init(action, getSecretKey(key));
			try(CipherInputStream cstream
					= new CipherInputStream(istream, cipher)) {
				int read	  = 0;
				long total	  = 0;
				byte[] buffer = new byte[bufferSize];
				while((read = cstream.read(buffer)) != -1) {
					ostream.write(buffer, 0, read);
					total += read;
				}
				return total;
			}
		} catch(Exception ex) {
		}
		return -1;
	}
	
	@Override
	public final long encrypt(InputStream istream, DataOutput ostream, byte[] key) {
		return datacrypt(istream, ostream, BUFFER_ENCRYPT_SIZE, key, Cipher.ENCRYPT_MODE);
	}
	
	@Override
	public final long decrypt(InputStream istream, DataOutput ostream, byte[] key) {
		return datacrypt(istream, ostream, BUFFER_DECRYPT_SIZE, key, Cipher.DECRYPT_MODE);
	}
	
	private static final long getOutputSize(long length, byte[] key, int mode) {
		try {
			Cipher cipher = getCipher();
			cipher.init(mode, getSecretKey(key));
			return cipher.getOutputSize((int) length);
		} catch(Exception ex) {
		}
		return -1;
	}
	
	@Override
	public final long getEncryptedSize(long length, byte[] key) {
		return getOutputSize(length, key, Cipher.ENCRYPT_MODE);
	}
	
	@Override
	public final long getDecryptedSize(long length, byte[] key) {
		return getOutputSize(length, key, Cipher.DECRYPT_MODE);
	}
	
	private static final Cipher initCipher(byte[] key, int mode) {
		try {
			Cipher cipher = getCipher();
			cipher.init(mode, getSecretKey(key));
			return cipher;
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public final Cipher initEncryptCipher(byte[] key) {
		return initCipher(key, Cipher.ENCRYPT_MODE);
	}
	
	@Override
	public final Cipher initDecryptCipher(byte[] key) {
		return initCipher(key, Cipher.DECRYPT_MODE);
	}
	
	private static final CipherInputStream inputStream(InputStream stream, int mode, byte[] key) {
		try {
			return new CipherInputStream(
				stream, initCipher(key, mode));
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public final CipherInputStream encryptInputStream(InputStream stream, byte[] key) {
		return inputStream(stream, Cipher.ENCRYPT_MODE, key);
	}
	
	@Override
	public final CipherInputStream decryptInputStream(InputStream stream, byte[] key) {
		return inputStream(stream, Cipher.DECRYPT_MODE, key);
	}
	
	private static final CipherOutputStream outputStream(OutputStream stream, int mode, byte[] key) {
		try {
			return new CipherOutputStream(
				stream, initCipher(key, mode));
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public final CipherOutputStream encryptOutputStream(OutputStream stream, byte[] key) {
		return outputStream(stream, Cipher.ENCRYPT_MODE, key);
	}
	
	@Override
	public final CipherOutputStream decryptOutputStream(OutputStream stream, byte[] key) {
		return outputStream(stream, Cipher.DECRYPT_MODE, key);
	}
	
	/* ------------------------------------------
	 * |										|
	 * |		   File cryptography			|
	 * |										|
	 * ------------------------------------------*/
	
	private static final int MODE_ENCRYPT = 1;
	private static final int MODE_DECRYPT = 2;
	
	private final boolean filecrypt(File input, File output, String key, int mode) {
		if(mode != MODE_ENCRYPT && mode != MODE_DECRYPT)
			return false;
		try {
			byte[] bkey = keyBytes(key);
			try(InputStream istream = new BufferedInputStream(
					new FileInputStream(input))) {
				try(OutputStream ostream = new BufferedOutputStream(
						new FileOutputStream(output))) {
					if(mode == MODE_ENCRYPT) encrypt(istream, ostream, bkey); else
					if(mode == MODE_DECRYPT) decrypt(istream, ostream, bkey);
					return true;
				}
			}
		} catch(Exception ex) {
		}
		return false;
	}
	
	@Override
	public final boolean encrypt(File input, File output, String key) {
		return filecrypt(input, output, key, MODE_ENCRYPT);
	}
	
	@Override
	public final boolean decrypt(File input, File output, String key) {
		return filecrypt(input, output, key, MODE_DECRYPT);
	}
}
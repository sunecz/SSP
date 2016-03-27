package sune.util.crypt;

import java.io.DataOutput;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

public interface CryptMethod {
	
	public byte[] encrypt(byte[] bytes, byte[] key);
	public String encrypt(String string, String key);
	public byte[] decrypt(byte[] bytes, byte[] key);
	public String decrypt(String string, String key);
	public byte[] keyBytes(String key);
	public String generateKey(int bits);
	public String weakKey();
	public String strongKey();
	
	/* ------------------------------------------
	 * |										|
	 * |		  Additional methods			|
	 * |										|
	 * ------------------------------------------*/
	
	public String getName();
	public int weakKeyBits();
	public int strongKeyBits();
	
	/* ------------------------------------------
	 * |										|
	 * |		  Stream cryptography			|
	 * |										|
	 * ------------------------------------------*/
	
	public void encrypt(InputStream istream, OutputStream ostream, byte[] key);
	public void decrypt(InputStream istream, OutputStream ostream, byte[] key);
	public long encrypt(InputStream istream, DataOutput ostream, byte[] key);
	public long decrypt(InputStream istream, DataOutput ostream, byte[] key);
	
	public long getEncryptedSize(long length, byte[] key);
	public long getDecryptedSize(long length, byte[] key);
	
	public Cipher initEncryptCipher(byte[] key);
	public Cipher initDecryptCipher(byte[] key);
	
	public CipherInputStream encryptInputStream(InputStream stream, byte[] key);
	public CipherInputStream decryptInputStream(InputStream stream, byte[] key);
	
	public CipherOutputStream encryptOutputStream(OutputStream stream, byte[] key);
	public CipherOutputStream decryptOutputStream(OutputStream stream, byte[] key);
	
	/* ------------------------------------------
	 * |										|
	 * |		   File cryptography			|
	 * |										|
	 * ------------------------------------------*/
	
	public boolean encrypt(File input, File output, String key);
	public boolean decrypt(File input, File output, String key);
}
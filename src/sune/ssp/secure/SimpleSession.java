package sune.ssp.secure;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import sune.ssp.util.Randomizer;
import sune.util.crypt.Crypt;
import sune.util.crypt.CryptRSA;

public class SimpleSession implements Session {
	
	private static final int DEFAULT_HASH_LENGTH = 32;
	
	public static enum SessionKey {
		RSA_1024 { @Override int bits() { return 1024; } },
		RSA_2048 { @Override int bits() { return 2048; } },
		RSA_4096 { @Override int bits() { return 4096; } };
		abstract int bits();
	}
	
	private final String 	 sessionHash;
	private final PublicKey  keyPublic;
	private final PrivateKey keyPrivate;
	private final SessionKey sessionKey;
	
	protected SimpleSession(String sessionHash,
			PublicKey keyPublic, PrivateKey keyPrivate, SessionKey sessionKey) {
		this.sessionHash = sessionHash;
		this.keyPublic 	 = keyPublic;
		this.keyPrivate  = keyPrivate;
		this.sessionKey  = sessionKey;
	}
	
	public static SimpleSession createSession() {
		return createSession(SessionKey.RSA_1024, DEFAULT_HASH_LENGTH);
	}
	
	public static SimpleSession createSession(SessionKey keySize) {
		return createSession(keySize, DEFAULT_HASH_LENGTH);
	}
	
	public static SimpleSession createSession(
			SessionKey keySize, int hashLength) {
		KeyPair keys = CryptRSA.generateKeyPair(keySize.bits());
		return new SimpleSession(
			Randomizer.randomString(hashLength),
			keys.getPublic(), keys.getPrivate(),
			keySize);
	}
	
	@Override
	public String encrypt(String string) {
		return Crypt.encrypt(string, keyPublic);
	}
	
	@Override
	public String decrypt(String string) {
		return Crypt.decrypt(string, keyPrivate);
	}
	
	@Override
	public String getHash() {
		return sessionHash;
	}
	
	@Override
	public PublicKey getPublicKey() {
		return keyPublic;
	}
	
	@Override
	public int keyBits() {
		return sessionKey.bits();
	}
	
	@Override
	public String getAlgorithmName() {
		// e.g. RSA_2048 => RSA 2048-bit
		return sessionKey.name().replace('_', ' ') + "-bit";
	}
}
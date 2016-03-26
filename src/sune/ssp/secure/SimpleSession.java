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
	
	public SimpleSession(String sessionHash,
			PublicKey keyPublic, PrivateKey keyPrivate) {
		this.sessionHash = sessionHash;
		this.keyPublic 	 = keyPublic;
		this.keyPrivate  = keyPrivate;
	}
	
	public static SimpleSession createSession() {
		return createSession(SessionKey.RSA_1024, DEFAULT_HASH_LENGTH);
	}
	
	public static SimpleSession createSession(SessionKey keySize) {
		return createSession(keySize, DEFAULT_HASH_LENGTH);
	}
	
	public static SimpleSession createSession(SessionKey keySize,
			int hashLength) {
		KeyPair keys = CryptRSA.generateKeyPair(keySize.bits());
		return createSession(
			keys.getPublic(), keys.getPrivate(),
			hashLength);
	}
	
	public static SimpleSession createSession(
			PublicKey keyPublic, PrivateKey keyPrivate,
			int hashLength) {
		return new SimpleSession(
			Randomizer.randomString(hashLength),
			keyPublic, keyPrivate);
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
}
package sune.ssp.crypt;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import sune.ssp.util.Randomizer;

public class SimpleSession implements Session {
	
	private static final int HASH_LENGTH = 32;
	
	private final String 	 sessionHash;
	private final PublicKey  keyPublic;
	private final PrivateKey keyPrivate;
	
	public SimpleSession(String sessionHash,
			PublicKey keyPublic, PrivateKey keyPrivate) {
		this.sessionHash = sessionHash;
		this.keyPublic 	 = keyPublic;
		this.keyPrivate  = keyPrivate;
	}
	
	public static SimpleSession createSession(
			PublicKey keyPublic, PrivateKey keyPrivate) {
		return new SimpleSession(
			Randomizer.randomString(HASH_LENGTH),
			keyPublic, keyPrivate);
	}
	
	public static SimpleSession createSession() {
		KeyPair keys = Crypt.generateRSAKeyPair();
		return new SimpleSession(
			Randomizer.randomString(HASH_LENGTH),
			keys.getPublic(), keys.getPrivate());
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
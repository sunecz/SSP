package sune.ssp.data;

import java.security.PublicKey;

import sun.security.rsa.RSAPublicKeyImpl;

public class PublicKeyData extends Data {
	
	private static final long serialVersionUID = 351742976187218025L;
	
	protected PublicKeyData(RSAPublicKeyImpl impl) {
		this((PublicKey) impl);
	}
	
	public PublicKeyData(PublicKey key) {
		super("publicKey", key);
	}
	
	public PublicKey getPublicKey() {
		return (PublicKey) getData("publicKey");
	}
}
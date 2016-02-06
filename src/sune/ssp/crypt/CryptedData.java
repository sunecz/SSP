package sune.ssp.crypt;

import java.security.Key;
import java.security.PublicKey;

import sun.security.rsa.RSAPublicKeyImpl;
import sune.ssp.data.Data;
import sune.ssp.util.Serialization;

public class CryptedData extends Data {
	
	private static final long serialVersionUID = -781582786177901750L;
	private static final String encrypt(PublicKey publicKey, SymmetricKey symmetricKey) {
		try {
			return Crypt.encrypt(
				Serialization
					.serializeToString(symmetricKey),
				publicKey);
		} catch(Exception ex) {
		}
		return null;
	}
	private static final String encrypt(SymmetricKey symmetricKey, Data data) {
		try {
			return Crypt.encryptAES(
				Serialization
					.serializeToString(data),
				symmetricKey.getKey());
		} catch(Exception ex) {
		}
		return null;
	}
	
	// Constructor for casting from Data object (with public key)
	protected CryptedData(String data, RSAPublicKeyImpl impl) {
		super("data", data,
			  "key",  (PublicKey) impl);
	}
	
	// Constructor for casting from Data object (with symmetric key)
	protected CryptedData(String data, SymmetricKey symmetricKey) {
		super("data", data,
			  "key",  symmetricKey);
	}
	
	public CryptedData(PublicKey publicKey, SymmetricKey symmetricKey) {
		super("data", encrypt(publicKey, symmetricKey),
			  "key",  publicKey);
	}
	
	public CryptedData(SymmetricKey symmetricKey, Data data) {
		super("data", encrypt(symmetricKey, data),
			  "key",  symmetricKey);
	}
	
	public String getData() {
		return (String) getData("data");
	}
	
	public Key getKey() {
		return (Key) getData("key");
	}
}
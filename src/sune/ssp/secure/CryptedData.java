package sune.ssp.secure;

import java.security.Key;
import java.security.PublicKey;

import sun.security.rsa.RSAPublicKeyImpl;
import sune.ssp.data.Data;
import sune.ssp.util.Serialization;
import sune.util.crypt.Crypt;
import sune.util.crypt.CryptMethod;

public class CryptedData extends Data {
	
	private static final long serialVersionUID = -781582786177901750L;
	private static final String encrypt(PublicKey publicKey, String symmetricKey) {
		try {
			return Crypt.encrypt(
				Serialization
					.serializeToString(symmetricKey),
				publicKey);
		} catch(Exception ex) {
		}
		return null;
	}
	private static final String encrypt(SymmetricKey symmetricKey, Data data,
			CryptMethod cryptMethod) {
		try {
			return cryptMethod.encrypt(
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
	
	public CryptedData(PublicKey publicKey, String symmetricKey) {
		super("data", encrypt(publicKey, symmetricKey),
			  "key",  publicKey);
	}
	
	public CryptedData(SymmetricKey symmetricKey, Data data, CryptMethod cryptMethod) {
		super("data", encrypt(symmetricKey, data, cryptMethod),
			  "key",  symmetricKey);
	}
	
	public String getData() {
		return (String) getData("data");
	}
	
	public Key getKey() {
		return (Key) getData("key");
	}
}
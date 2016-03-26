package sune.ssp.secure;

import java.security.PublicKey;

public interface Session {
	
	public String encrypt(String string);
	public String decrypt(String string);
	public String getHash();
	public PublicKey getPublicKey();
}
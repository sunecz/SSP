package sune.util.crypt;

import java.security.Key;

public final class SymmetricKey implements Key {
	
	private static final long serialVersionUID = 7602143102938323381L;
	
	private final String key;
	public SymmetricKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}

	@Override
	public String getAlgorithm() {
		return null;
	}

	@Override
	public String getFormat() {
		return null;
	}

	@Override
	public byte[] getEncoded() {
		return null;
	}
}
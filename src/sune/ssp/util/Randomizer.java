package sune.ssp.util;

import java.security.SecureRandom;

public final class Randomizer {
	
	static final SecureRandom RANDOM;
	static {
		RANDOM = new SecureRandom(uniqueSeed(null));
	}
	
	static final byte[] uniqueSeed(SecureRandom random) {
		long value = (System.nanoTime() ^ 1820756412364412L) ^
					 (System.nanoTime() ^ 9163131841464168L) ^
					 (System.nanoTime() << 16) ^
					 (System.nanoTime() << 32) ^
					 (System.nanoTime() << 48) ^
					 (System.nanoTime());
		if(random != null) value ^= random.nextLong();
		byte[] raw = new byte[8];
		for(int i = 0, k = 0; i < 64; i+=8, ++k) {
			raw[k] = (byte) ((value >> i) & 0xff);
		}
		return raw;
	}
	
	public static final SecureRandom createSecure() {
		return new SecureRandom(uniqueSeed(RANDOM));
	}
	
	public static final long nextLong() {
		return createSecure().nextLong();
	}
	
	public static final long nextPositiveLong() {
		// Mask the long without the sign bit
		return createSecure().nextLong() & 0x7ffffffffffffffL;
	}
	
	public static final int nextInt() {
		return createSecure().nextInt();
	}
	
	public static final int nextPositiveInt() {
		// Mask the integer without the sign bit
		return createSecure().nextInt() & 0x7fffffff;
	}
	
	public static final SecureRandom createSecureSHA1() {
		try {
			SecureRandom random
				= SecureRandom.getInstance(
					"SHA1PRNG", "SUN");
			random.setSeed(uniqueSeed(RANDOM));
			return random;
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final SecureRandom createSecureStrong() {
		try {
			SecureRandom random
				= SecureRandom.getInstanceStrong();
			random.setSeed(uniqueSeed(RANDOM));
			return random;
		} catch(Exception ex) {
		}
		return null;
	}
	
	static final String 	  STRING 		= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static final int		  STRING_MAX	= STRING.length();
	static final SecureRandom STRING_RANDOM = createSecure();
	static final char randomCharacter(SecureRandom random) {
		return STRING.charAt(random.nextInt(STRING_MAX));
	}
	
	public static final String randomString(int length, SecureRandom random) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < length; ++i)
			sb.append(randomCharacter(random));
		return sb.toString();
	}
	
	public static final String randomString(int length) {
		return randomString(length, STRING_RANDOM);
	}
}
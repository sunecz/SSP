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
	
	public static final int nextInt() {
		return createSecure().nextInt();
	}
}
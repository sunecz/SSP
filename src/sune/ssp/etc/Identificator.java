package sune.ssp.etc;

import java.security.SecureRandom;
import java.util.UUID;

import sune.ssp.util.Randomizer;

public class Identificator {
	
	private static final SecureRandom RANDOM;
	static {
		RANDOM = Randomizer.createSecureStrong();
	}
	
	private final UUID uuid;
	private final Object value;
	
	public Identificator(UUID uuid, Object value) {
		this.uuid  = uuid;
		this.value = value;
	}
	
	public static final UUID random() {
		return UUID.randomUUID();
	}
	
	public static final UUID random0() {
		return create(random().toString());
	}
	
	public static final UUID create(String string) {
		UUID randUUID  = random();
		long bitsMost  = randUUID.getMostSignificantBits();
		long bitsLeast = randUUID.getLeastSignificantBits();
		
		long rand0 = RANDOM.nextLong();
		long rand1 = RANDOM.nextLong();
		rand0 &= bitsMost;
		rand1 &= bitsLeast;
		
		if(string != null) {
			char[] chars = string.toCharArray();
			for(int i = 0, n = 0, l = chars.length; i < l; ++i) {
				char c  = chars[i];
				byte b0 = (byte) ((c) 	   & 0xff);
				byte b1 = (byte) ((c >> 8) & 0xff);
				rand0 = ((rand0 >>> n) ^ b0) << n;
				rand1 = ((rand1 >>> n) ^ b1) << n;
				if((n += 2) == 64) {
					n = 0;
				}
			}
		}
		
		if(rand0 < 0) rand0 = -rand0;
		if(rand1 > 0) rand1 = -rand1;
		bitsMost  ^= rand0;
		bitsLeast ^= rand1;
		
		UUID randUUID0 = random();
		bitsMost  += randUUID0.getLeastSignificantBits();
		bitsLeast -= randUUID0.getMostSignificantBits();
		return new UUID(bitsMost, bitsLeast);
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public Object getValue() {
		return value;
	}
}
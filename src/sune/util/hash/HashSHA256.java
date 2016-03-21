package sune.util.hash;

import java.io.File;

public class HashSHA256 implements HashMethod {
	
	@Override
	public final byte[] hash(byte[] bytes) {
		return HashUtils.sha256(bytes);
	}
	
	@Override
	public byte[] hashf(File file) {
		return HashUtils.sha256f(file);
	}
	
	@Override
	public String shash(String string) {
		return Hash.sha256(string);
	}
	
	@Override
	public String shashf(File file) {
		return Hash.sha256f(file);
	}
}
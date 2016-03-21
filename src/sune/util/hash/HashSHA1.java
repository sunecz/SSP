package sune.util.hash;

import java.io.File;

public class HashSHA1 implements HashMethod {
	
	@Override
	public final byte[] hash(byte[] bytes) {
		return HashUtils.sha1(bytes);
	}
	
	@Override
	public byte[] hashf(File file) {
		return HashUtils.sha1f(file);
	}
	
	@Override
	public String shash(String string) {
		return Hash.sha1(string);
	}
	
	@Override
	public String shashf(File file) {
		return Hash.sha1f(file);
	}
}
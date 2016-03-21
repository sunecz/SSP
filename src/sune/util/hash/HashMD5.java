package sune.util.hash;

import java.io.File;

public class HashMD5 implements HashMethod {
	
	@Override
	public final byte[] hash(byte[] bytes) {
		return HashUtils.md5(bytes);
	}
	
	@Override
	public byte[] hashf(File file) {
		return HashUtils.md5f(file);
	}
	
	@Override
	public String shash(String string) {
		return Hash.md5(string);
	}
	
	@Override
	public String shashf(File file) {
		return Hash.md5f(file);
	}
}
package sune.util.hash;

import java.io.File;

public interface HashMethod {
	
	public byte[] hash(byte[] bytes);
	public byte[] hashf(File file);
	
	public String shash(String string);
	public String shashf(File file);
}
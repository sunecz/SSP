/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.ssp.util;

import java.io.File;
import java.nio.charset.Charset;

public class PathSystem {
	
	public static final String DIRECTORY;
	
	static {
		DIRECTORY = encodeString(getCurrentDirectory());
	}
	
	static final Charset CHARSET = Charset.forName("UTF-8");
	static String encodeString(String string) {
		try {
			return new String(
				CHARSET
					.encode(string)
					.array(),
				CHARSET)
					.trim();
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String getCurrentDirectory() {
		if(DIRECTORY == null) {
			try {
				return new File(
					PathSystem.class
						.getProtectionDomain()
						.getCodeSource()
						.getLocation()
						.toURI()
						.getPath())
					.getParentFile()
					.getAbsolutePath()
					.replace("\\", "/") + "/";
			} catch(Exception ex) {
				return new File("")
					.getAbsolutePath()
					.replace("\\", "/") + "/";
			}
		}
		return DIRECTORY;
	}
	
	public static final String getFullPath(String path) {
		return (getCurrentDirectory() + path).replace("\\", "/");
	}
}
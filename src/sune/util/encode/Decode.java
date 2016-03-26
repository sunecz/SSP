package sune.util.encode;

import java.nio.charset.Charset;

public class Decode {
	
	static final Charset CHARSET = Charset.forName("UTF-8");
	static final String encodeUTF8(String string) {
		return new String(string.getBytes(), CHARSET);
	}
	
	public static final String base64(String string) {
		return Base64.decodeString(encodeUTF8(string));
	}
	
	public static final String base64URL(String string) {
		return Base64URL.decodeString(encodeUTF8(string));
	}
}
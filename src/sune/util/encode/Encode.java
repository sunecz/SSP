package sune.util.encode;

import java.nio.charset.Charset;

public class Encode {
	
	static final Charset CHARSET = Charset.forName("UTF-8");
	static final String encodeUTF8(String string) {
		return new String(string.getBytes(), CHARSET);
	}
	
	public static final String base64(String string) {
		return Base64.encodeString(encodeUTF8(string));
	}
	
	public static final String base64URL(String string) {
		return Base64URL.encodeString(encodeUTF8(string));
	}
}
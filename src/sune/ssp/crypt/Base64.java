package sune.ssp.crypt;

import java.nio.charset.Charset;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64 {
	
	private static final BASE64Encoder ENCODER;
	private static final BASE64Decoder DECODER;
	private static final Charset	   CHARSET;
	
	static {
		ENCODER = new BASE64Encoder();
		DECODER = new BASE64Decoder();
		CHARSET = Crypt.CHARSET;
	}
	
	public static final String encode(byte[] bytes) {
		try {
			return ENCODER.encode(bytes);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String encodeString(String string) {
		return encode(string.getBytes(CHARSET));
	}
	
	public static final byte[] decode(String string) {
		try {
			return DECODER.decodeBuffer(string);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String decodeString(String string) {
		return new String(decode(string), CHARSET);
	}
}
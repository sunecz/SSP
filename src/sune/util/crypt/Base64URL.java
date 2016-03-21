package sune.util.crypt;

import java.nio.charset.Charset;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class Base64URL {
	
	private static final Encoder ENCODER;
	private static final Decoder DECODER;
	private static final Charset CHARSET;
	
	static {
		ENCODER = java.util.Base64.getUrlEncoder();
		DECODER = java.util.Base64.getUrlDecoder();
		CHARSET = Crypt.CHARSET;
	}
	
	public static final byte[] encode(byte[] bytes) {
		try {
			return ENCODER.encode(bytes);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String encodeString(byte[] bytes) {
		return new String(encode(bytes), CHARSET);
	}
	
	public static final String encodeString(String string) {
		return encodeString(string.getBytes(CHARSET));
	}
	
	public static final byte[] encode0(String string) {
		return encode(string.getBytes(CHARSET));
	}
	
	public static final byte[] decode(byte[] bytes) {
		try {
			return DECODER.decode(bytes);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String decodeString(byte[] bytes) {
		return new String(decode(bytes), CHARSET);
	}
	
	public static final String decodeString(String string) {
		return decodeString(string.getBytes(CHARSET));
	}
	
	public static final byte[] decode0(String string) {
		return decode(string.getBytes(CHARSET));
	}
}
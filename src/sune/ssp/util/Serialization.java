package sune.ssp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public final class Serialization {
	
	public static final byte[] serialize(Object object) {
		if(object == null) {
			throw new IllegalArgumentException(
				"Object cannot be null!");
		}
		try {
			try(ByteArrayOutputStream bo
					= new ByteArrayOutputStream()) {
				try(ObjectOutputStream so
						= new ObjectOutputStream(bo)) {
					so.writeObject(object);
					so.flush();
					return Base64.getEncoder()
						.encode(bo.toByteArray());
				}
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String serializeToString(Object object) {
		if(object == null) {
			throw new IllegalArgumentException(
				"Object cannot be null!");
		}
		try {
			return new String(
				serialize(object), "UTF-8");
		} catch(Exception ex) {
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T deserialize(byte[] bytes) {
		if(bytes == null) {
			throw new IllegalArgumentException(
				"Bytes cannot be null!");
		}
		try {
			byte[] bytes0 = Base64.getDecoder()
				.decode(bytes);
			try(ByteArrayInputStream bi
					= new ByteArrayInputStream(bytes0)) {
				try(ObjectInputStream si
						= new ObjectInputStream(bi)) {
					return (T) si.readObject();
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static final <T> T deserializeFromString(String string) {
		if(string == null) {
			throw new IllegalArgumentException(
				"String cannot be null!");
		}
		return deserialize(string.getBytes());
	}
}
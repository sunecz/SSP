package sune.ssp.data;

public final class Value {
	
	private final Object value;
	public Value(Object value) {
		this.value = value;
	}
	
	public boolean booleanValue() {
		return (boolean) value;
	}
	
	public byte byteValue() {
		return (byte) value;
	}
	
	public char charValue() {
		return (char) value;
	}
	
	public short shortValue() {
		return (short) value;
	}
	
	public int intValue() {
		return (int) value;
	}
	
	public long longValue() {
		return (long) value;
	}
	
	public float floatValue() {
		return (float) value;
	}
	
	public double doubleValue() {
		return (double) value;
	}
	
	public String stringValue() {
		return value != null ? value.toString() : null;
	}
	
	public Object value() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T value(Class<T> clazz) {
		try {
			if(clazz.isAssignableFrom(value.getClass())) {
				return (T) value;
			}
		} catch(Exception ex) {
		}
		return null;
	}
}
package sune.ssp.util;

public class TypeUtils {
	
	public static Class<?>[] recognizeClasses(Object... arguments) {
		int length 		   = arguments.length;
		Class<?>[] classes = new Class<?>[length];
		for(int i = 0; i < length; ++i) {
			Object argument;
			if((argument = arguments[i]) == null) {
				classes[i] = Object.class;
				continue;
			}
			Class<?> clazz = argument.getClass();
			classes[i] 	   = toPrimitive(clazz);
		}
		return classes;
	}
	
	public static Class<?> toPrimitive(Class<?> clazz) {
		if(clazz == Boolean.class) 	 return boolean.class;
        if(clazz == Character.class) return char.class;
        if(clazz == Byte.class) 	 return byte.class;
        if(clazz == Short.class) 	 return short.class;
        if(clazz == Integer.class) 	 return int.class;
        if(clazz == Long.class) 	 return long.class;
        if(clazz == Float.class) 	 return float.class;
        if(clazz == Double.class) 	 return double.class;
        if(clazz == Void.class) 	 return void.class;
		return clazz;
	}
}
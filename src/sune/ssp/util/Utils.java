package sune.ssp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {
	
	public static boolean sleep(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch(Exception ex) {
		}
		
		return false;
	}
	
	public static double round(double number, int decimals) {
		double tens = Math.pow(10, decimals);
		return Math.floor(number * tens + 0.5) / tens;
	}
	
	public static String fancyEnumName(Enum<?> object) {
		String typeName = object.name().replaceAll("_", " ");
		String[] split  = typeName.split(" ");
		
		StringBuilder builder = new StringBuilder();
		for(String part : split)
			builder.append(part.substring(0, 1) +
				part.substring(1).toLowerCase()).append(" ");
		
		return builder.toString().trim();
	}
	
	public static <K, V> List<V> mapToList(Map<K, V> map) {
		List<V> list = new ArrayList<>();
		for(Entry<K, V> entry : map.entrySet())
			list.add(entry.getValue());
		return list;
	}
	
	public static int[] toIntArray(Integer[] a) {
		int[] arr = new int[a.length];
		for(int i = 0,
				l = a.length; i < l; ++i)
			arr[i] = a[i];
		return arr;
	}
	
	public static <T> T[] copy(T[] array) {
		return array == null ? null :
			Arrays.copyOf(array, array.length);
	}
	
	@SafeVarargs
	public static <T> List<T> toList(T... array) {
		List<T> list = new ArrayList<>();
		for(int i = 0,
				l = array.length; i < l; ++i)
			list.add(array[i]);
		return list;
	}
}
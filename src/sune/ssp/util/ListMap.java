package sune.ssp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ListMap<T, E> {
	
	private final Map<T, List<E>> map = new LinkedHashMap<>();
	
	public List<E> ensure(T name) {
		if(name == null) {
			throw new IllegalArgumentException(
				"Name cannot be null!");
		}
		if(!has(name)) {
			add(name, new ArrayList<>());
		}
		return get(name);
	}
	
	public void add(T name, List<E> values) {
		if(name == null || values == null) {
			throw new IllegalArgumentException(
				"Name and values cannot be null!");
		}
		synchronized(map) {
			map.put(name, values);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void add(T name, E... values) {
		add(name, Arrays.asList(values));
	}
	
	public void append(T name, E value) {
		if(name == null || value == null) {
			throw new IllegalArgumentException(
				"Name and value cannot be null!");
		}
		if(has(name)) {
			get(name).add(value);
		} else {
			add(name,
				Arrays.asList(value));
		}
	}
	
	public boolean has(T name) {
		if(name == null) {
			throw new IllegalArgumentException(
				"Name cannot be null!");
		}
		synchronized(map) {
			return map.containsKey(name);
		}
	}
	
	public List<E> get(T name) {
		if(name == null) {
			throw new IllegalArgumentException(
				"Name cannot be null!");
		}
		synchronized(map) {
			return map.get(name);
		}
	}
	
	public void remove(T name) {
		if(name == null) {
			throw new IllegalArgumentException(
				"Name cannot be null!");
		}
		synchronized(map) {
			map.remove(name);
		}
	}
	
	public void removeValue(E value) {
		if(value == null) {
			throw new IllegalArgumentException(
				"Value cannot be null!");
		}
		synchronized(map) {
			for(List<E> list : map.values()) {
				list.remove(value);
			}
		}
	}
	
	public int countForValue(E value) {
		if(value == null) {
			throw new IllegalArgumentException(
				"Value cannot be null!");
		}
		synchronized(map) {
			int count = 0;
			for(List<E> list : map.values()) {
				if(list.contains(value))
					++count;
			}
			return count;
		}
	}
	
	public void clear() {
		synchronized(map) {
			map.clear();
		}
	}
}
package sune.ssp.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sune.ssp.util.TypeUtils;

public class Data implements Serializable, Comparable<Object> {
	
	private static final long serialVersionUID = 6826907774920347260L;
	protected final Map<String, Object> values;
	
	private static final String PROPERTY_CLASS 	  = "class";
	private static final String PROPERTY_SENDERIP = "senderIP";
	private static final String PROPERTY_DATETIME = "dateTime";
	
	private static final Map<String, Object> mk_propMap(Object... values) {
		if((values.length & 1) == 1) {
			throw new IllegalArgumentException(
				"Number of values is incorrect! Should be even.");
		}
		
		String key = null;
		Map<String, Object> map = new LinkedHashMap<>();
		for(int i = 0, k = 0, l = values.length; i < l; ++i) {
			switch(k) {
				case 0:
					Object value = values[i];
					key = value != null ?
						value.toString() : null;
					break;
				case 1:
					if(key != null) {
						map.put(key, values[i]);
						key = null;
					}
					break;
			}
			
			if(++k == 2) k = 0;
		}
		
		return map;
	}
	
	public Data(Object... values) {
		this.values = mk_propMap(values);
		this.values.put(PROPERTY_CLASS, getClass());
	}
	
	protected Data(Map<String, Object> values) {
		this.values = values;
	}
	
	protected void setSenderIP(String senderIP) {
		values.put(PROPERTY_SENDERIP, senderIP);
	}
	
	protected void setDateTime(String dateTime) {
		values.put(PROPERTY_DATETIME, dateTime);
	}
	
	public Object getData(String name) {
		return values.get(name);
	}
	
	public String getSenderIP() {
		return new Value(getData(PROPERTY_SENDERIP)).stringValue();
	}
	
	public String getDateTime() {
		return new Value(getData(PROPERTY_DATETIME)).stringValue();
	}
	
	protected Map<String, Object> getPropMap() {
		return values;
	}
	
	/**
	 * Use <code>data.cast() instanceof [clazz]</code> instead.*/
	@Deprecated
	public boolean instanceOf(Class<? extends Data> clazz) {
		return ((Class<?>) getData(PROPERTY_CLASS)).getName().equals(clazz.getName());
	}
	
	@Override
	public int compareTo(Object o) {
		if(!(o instanceof Data))
			return -1;
		Data d = (Data) o;
		if(d.values != values)
			return -1;
		return 0;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String toString() {
		return String.format(
			"Transferable data (type=%s,size=%d)",
			((Class<? extends Data>) values.get(PROPERTY_CLASS)).getName(),
			values.size());
	}
	
	@SuppressWarnings("unchecked")
	private Data cast0() {
		return cast(((Class<? extends Data>) values.get(PROPERTY_CLASS)));
	}
	
	public Data cast() {
		return cast0();
	}
	
	public <T extends Data> T cast(Class<T> clazz) {
		return cast(this, clazz);
	}
	
	private static final <T extends Data> T cast(Data data, Class<T> clazz) {
		try {
			Map<String, Object> map = data.getPropMap();
			List<Object> list		= new ArrayList<>();
			for(Entry<String, Object> e : map.entrySet()) {
				String name = e.getKey();
				if(!name.equals(PROPERTY_SENDERIP) &&
				   !name.equals(PROPERTY_DATETIME) &&
				   !name.equals(PROPERTY_CLASS)) {
					list.add(e.getValue());
				}
			}
			
			Object[] values    = list.toArray();
			Class<?>[] classes = TypeUtils.recognizeClasses(values);
			T instance 		   = (T) clazz.getConstructor(classes).newInstance(values);
			instance.setSenderIP(data.getSenderIP());
			instance.setDateTime(data.getDateTime());
			return instance;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
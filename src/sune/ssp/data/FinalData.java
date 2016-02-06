package sune.ssp.data;

import java.io.Serializable;
import java.util.Map;

import sune.ssp.util.DateHelper;

public final class FinalData implements Serializable, Comparable<Object> {
	
	private static final long serialVersionUID = 2080604495909681768L;
	protected final Object value;
	protected final String senderIP;
	protected final String dateTime;
	
	public FinalData(Object value, String senderIP) {
		this(value, senderIP, DateHelper.getCurrentDate());
	}
	
	@SuppressWarnings("unchecked")
	private FinalData(Object value, String senderIP, String dateTime) {
		if(value instanceof Map<?, ?>) {
			Map<Object, Object> map
				= (Map<Object, Object>) value;
			map.put("senderIP", senderIP);
			map.put("dateTime", dateTime);
		}
		this.value 	  = value;
		this.senderIP = senderIP;
		this.dateTime = dateTime;
	}
	
	public Object getData() {
		return value;
	}
	
	public String getSenderIP() {
		return senderIP;
	}
	
	public String getDateTime() {
		return dateTime;
	}
	
	@Override
	public int compareTo(Object o) {
		if(!(o instanceof FinalData))
			return -1;
		FinalData d = (FinalData) o;
		if(d.value    != value)    return -1;
		if(d.dateTime != dateTime) return -1;
		if(d.senderIP != senderIP) return -1;
		return 0;
	}
	
	@Override
	public String toString() {
		return value != null ? value.toString() : null;
	}
	
	@SuppressWarnings("unchecked")
	private Data toData0() {
		return new Data(
			(Map<String, Object>) value);
	}
	
	public final Data toData() {
		return toData0();
	}
	
	public static final FinalData create(String senderIP, Data data) {
		return new FinalData(data.getPropMap(), senderIP);
	}
	
	public static final FinalData create(FinalData fdata, Data data) {
		return new FinalData(data.getPropMap(), fdata.senderIP, fdata.dateTime);
	}
}
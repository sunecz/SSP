package sune.ssp.data;

import java.io.Serializable;
import java.util.Map;

import sune.ssp.etc.Identificator;
import sune.ssp.util.DateHelper;

public final class FinalData implements Serializable, Comparable<Object> {
	
	private static final long serialVersionUID = 2080604495909681768L;
	protected final Object value;
	protected final String senderIP;
	protected final String dateTime;
	protected final String receiver;
	protected final String uuid;
	
	@SuppressWarnings("unchecked")
	private FinalData(Object value, String senderIP, String uuid, String dateTime, String receiver) {
		if(value instanceof Map<?, ?>) {
			Map<Object, Object> map
				= (Map<Object, Object>) value;
			map.put("senderIP", senderIP);
			map.put("dateTime", dateTime);
			map.put("receiver", receiver);
			map.put("uuid", 	uuid);
		}
		this.value 	  = value;
		this.senderIP = senderIP;
		this.dateTime = dateTime;
		this.receiver = receiver;
		this.uuid	  = uuid;
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
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getUUID() {
		return uuid;
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
		return new Data((Map<String, Object>) value);
	}
	
	public final Data toData() {
		return toData0();
	}
	
	public static final FinalData create(Identificator identificator, String receiver, Data data) {
		if(data == null) {
			throw new IllegalArgumentException(
				"Data cannot be null!");
		}
		String senderIP = "";
		String uuid		= "";
		if(identificator != null) {
			senderIP = (String) identificator.getValue();
			uuid	 = identificator.getUUID().toString();
		}
		return new FinalData(data.getPropMap(), senderIP,
				uuid, DateHelper.getCurrentDate(), receiver);
	}
	
	public static final FinalData create(String senderIP, String uuid, String receiver, Data data) {
		if(data == null) {
			throw new IllegalArgumentException(
				"Data cannot be null!");
		}
		return new FinalData(data.getPropMap(), (String) senderIP,
				uuid, DateHelper.getCurrentDate(), receiver);
	}
	
	public static final FinalData create(FinalData fdata, Data data) {
		if(fdata == null) {
			throw new IllegalArgumentException(
				"Final data cannot be null!");
		}
		if(data == null) {
			throw new IllegalArgumentException(
				"Data cannot be null!");
		}
		return new FinalData(data.getPropMap(), fdata.senderIP,
				fdata.uuid, fdata.dateTime, fdata.receiver);
	}
}
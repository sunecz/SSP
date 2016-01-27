package sune.ssp.file;

import sune.ssp.util.DateHelper;

public class FileReceiver {
	
	private Receiver receiver;
	
	private String senderIP;
	private String hash;
	private String time;
	private String name;
	
	private long current;
	private long total;
	
	public FileReceiver(String hash, String name, long size, String senderIP) {
		this.senderIP = senderIP;
		this.hash	  = hash;
		this.time	  = DateHelper.getCurrentDate();
		this.name 	  = name;
		this.current  = 0;
		this.total 	  = size;
	}
	
	public void init(Receiver receiver) {
		this.receiver = receiver;
		this.receiver.begin();
	}
	
	public void receive(byte[] data) {
		current += data.length;
		receiver.receive(data);
		if(isDone()) {
			receiver.end();
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSenderIP() {
		return senderIP;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getTime() {
		return time;
	}
	
	public String getName() {
		return name;
	}
	
	public long getCurrentSize() {
		return current;
	}
	
	public long getTotalSize() {
		return total;
	}
	
	public boolean isDone() {
		return current >= total;
	}
}
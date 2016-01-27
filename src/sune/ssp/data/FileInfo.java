package sune.ssp.data;

import sune.ssp.util.DateHelper;

public class FileInfo {
	
	private String hash;
	private String time;
	private String name;
	private String senderIP;
	
	public FileInfo(String hash, String name, String senderIP) {
		this.hash 	  = hash;
		this.time	  = DateHelper.getCurrentDate();
		this.name 	  = name;
		this.senderIP = senderIP;
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
	
	public String getSenderIP() {
		return senderIP;
	}
}
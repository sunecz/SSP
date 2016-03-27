package gui;
import sune.ssp.file.TransferType;
import sune.ssp.util.UnitHelper;
import sune.ssp.util.Utils;

public class FileTableInfo {
	
	private String sender;
	private String hash;
	private String name;
	private long current;
	private long total;
	
	private TransferType type;
	
	private long lastTime;
	private long tempTime;
	private long lastSize;
	private long speed;
	
	public FileTableInfo(String sender, String hash, String name, long total, TransferType type) {
		this.sender  = sender;
		this.hash	 = hash;
		this.name	 = name;
		this.current = 0;
		this.total	 = total;
		this.type	 = type;
	}
	
	public void update(long current) {
		long time 	  = System.nanoTime();
		long diffTime = time - lastTime;
		
		tempTime += diffTime;
		if(tempTime > 1e9) {
			speed 	 = current - lastSize;
			lastSize = current;
			tempTime = 0;
		}
		
		this.current = current;
		lastTime = time;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getSender() {
		return sender;
	}
	
	public long getCurrentSize() {
		return current;
	}
	
	public long getTotalSize() {
		return total;
	}
	
	public TransferType getTransferType() {
		return type;
	}
	
	public void setName(String name) {}
	public void setType(String type) {}
	public void setStatus(String status) {}
	
	public String getName() { return name; }
	public String getType() { return Utils.fancyEnumName(type); }
	public String getStatus() {
		return String.format("%s/s (%.2f%%)",
			UnitHelper.formatSize(this.speed, 2),
			total > 0 ? (current * 100.0) / total : 100.0);
	}
}
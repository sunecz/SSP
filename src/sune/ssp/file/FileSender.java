package sune.ssp.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import sune.ssp.data.Data;
import sune.ssp.data.FileData;
import sune.ssp.util.DateHelper;
import sune.ssp.util.HashHelper;

public class FileSender {
	
	private Sender sender;
	private byte[] buffer;
	private BufferedInputStream stream;
	
	private File file;
	private String hash;
	private String time;
	
	private long current;
	private long total;
	
	public FileSender(File file) {
		this.file 	 = file;
		this.hash	 = HashHelper.sha1(file);
		this.time	 = DateHelper.getCurrentDate();
		this.current = 0;
		this.total 	 = file.length();
	}
	
	private FileSender(File file, String hash, String time, long current,
			long total, Sender sender) {
		this.file 	 = file;
		this.hash 	 = hash;
		this.time 	 = time;
		this.current = current;
		this.total 	 = total;
		this.sender  = sender;
	}
	
	public void init(Sender sender, int bufferSize) {
		this.sender = sender;
		this.buffer = new byte[bufferSize];
		try {
			this.stream = new BufferedInputStream(
				new FileInputStream(file));
			sender.begin();
		} catch(Exception ex) {
		}
	}
	
	public void sendNext() {
		try {
			int read;
			if((read = stream.read(buffer)) != -1) {
				sender.send(new FileData(hash,
					Arrays.copyOf(buffer, read),
					total));
				current += read;
			} else {
				close();
				sender.end();
			}
		} catch(Exception ex) {
		}
	}
	
	private void close0() {
		try {
			stream.close();
			stream = null;
		} catch(Exception ex) {
		} finally {
			if(stream != null) {
				close0();
			}
		}
	}
	
	public void close() {
		if(stream != null) {
			close0();
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getTime() {
		return time;
	}
	
	public long getCurrentSize() {
		return current;
	}
	
	public long getTotalSize() {
		return total;
	}
	
	public Sender getSender() {
		return sender;
	}
	
	/* This method is used for getting the client's ip address
	 * at file termination.*/
	public FileSender copyFor(String uuid) {
		Sender sendercopy = new Sender() {
			@Override public void begin() 			   { sender.begin(); 	   }
			@Override public void end() 			   { sender.end(); 		   }
			@Override public void send(Data data) 	   { sender.send(data);    }
			@Override public void receive(byte[] data) { sender.receive(data); }
			
			@Override
			public String getSender() {
				return uuid;
			}
		};
		return new FileSender(
			file, hash, time, current, total, sendercopy);
	}
}
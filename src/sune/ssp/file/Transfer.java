package sune.ssp.file;

import sune.ssp.data.Data;

public interface Transfer {
	
	public void begin();
	public void end();
	public void send(Data data);
	public void receive(byte[] data);
	public String getSender();
}
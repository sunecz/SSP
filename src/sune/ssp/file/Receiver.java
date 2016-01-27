package sune.ssp.file;

import sune.ssp.data.Data;

public interface Receiver extends Transfer {
	
	@Override
	public default void send(Data data) {	
	}
}
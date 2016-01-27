package sune.ssp.file;

public interface Sender extends Transfer {
	
	@Override
	public default void receive(byte[] data) {
	}
}
package sune.ssp.data;

import sune.ssp.file.TransferType;

public class TerminationData extends Data {
	
	private static final long serialVersionUID = -5605013040673618586L;
	
	public TerminationData(String hash, TransferType type) {
		super("hash", hash,
			  "type", type);
	}
	
	public String getHash() {
		return new Value(getData("hash")).stringValue();
	}
	
	public TransferType getType() {
		return new Value(getData("type")).value(TransferType.class);
	}
}
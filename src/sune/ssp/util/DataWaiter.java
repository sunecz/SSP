package sune.ssp.util;

import sune.ssp.data.Data;

public class DataWaiter {
	
	private Waiter waiter;
	private Data data;
	
	public DataWaiter(Waiter waiter, Data data) {
		this.waiter = waiter;
		this.data   = data;
	}
	
	public Waiter getWaiter() {
		return waiter;
	}
	
	public Data getData() {
		return data;
	}
}
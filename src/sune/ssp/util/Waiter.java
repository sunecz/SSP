package sune.ssp.util;

public abstract class Waiter {
	
	private long time;
	private long current;
	private boolean stopped;
	private boolean accepted;
	private boolean refused;
	private boolean waiting;
	
	private Thread threadWait;
	private Runnable runWait = (() -> {
		while(!stopped && !accepted &&
			  !refused &&  current < time) {
			if(!waiting) ++current;
			Utils.sleep(1);
		}
		
		if(accepted) accepted();
		if(refused)	 refused();
	});
	
	public Waiter(long time) {
		this.time = time;
	}
	
	public abstract void accepted();
	public abstract void refused();
	public void waitingStateChanged(boolean value) {};
	
	public void start() {
		this.current 	= 0;
		this.threadWait = new Thread(runWait);
		this.threadWait.start();
	}
	
	public void stop() {
		stopped = true;
	}
	
	public void accept() {
		accepted = true;
	}
	
	public void refuse() {
		refused = true;
	}
	
	public void setWaitingState(boolean flag) {
		waitingStateChanged(waiting = flag);
	}
}
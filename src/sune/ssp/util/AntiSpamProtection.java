package sune.ssp.util;

import sune.ssp.data.Data;
import sune.ssp.data.Message;

public class AntiSpamProtection {
	
	private long maxTime;
	private int  maxAttempts;
	
	private long lastTime;
	private int  attempts;
	
	/**
	 * Creates new Anti-spam protection object with default values.*/
	public AntiSpamProtection() {
		this(300, 5);
	}
	
	/**
	 * Creates new Anti-spam protection object with defined maximum
	 * time in which a mesasge is counted as a spam, and with 
	 * defined maximum amount of spams that are allowed to be sent
	 * by a client.
	 * @param maxTime The maximum time in which a message is counted
	 * as a spam (in milliseconds).
	 * @param maxAttempts The maximum amount of spams that are allowed
	 * to be sent by a client.*/
	public AntiSpamProtection(int maxTime, int maxAttempts) {
		this.maxTime 	 = maxTime * 1000000L;
		this.maxAttempts = maxAttempts;
		this.lastTime 	 = System.nanoTime();
	}
	
	/**
	 * Checks if the given data are of the right type and if in this call that
	 * data are counted as a spam. If so, the number of attempts is increased.
	 * If the number of attempts is same or higher as the maximum amount of spams
	 * that are allowed to be sent by a client, this method returns true, otherwise false.
	 * @param data Data to be check
	 * @return True, if the maximum number of spams has been reached,
	 * otherwise false.*/
	public synchronized boolean check(Data data) {
		long time = System.nanoTime();
		if(checkData(data)) {
			if((time - lastTime) <= maxTime) {
				if(++attempts >= maxAttempts) {
					return true;
				}
			} else {
				attempts = 0;
			}
		}
		
		lastTime = time;
		return false;
	}
	
	protected boolean checkData(Data data) {
		return data instanceof Message;
	}
	
	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime * 1000000L;
	}
	
	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}
	
	public int getMaxTime() {
		return (int) (maxTime / 1000000L);
	}
	
	public int getMaxAttempts() {
		return maxAttempts;
	}
}
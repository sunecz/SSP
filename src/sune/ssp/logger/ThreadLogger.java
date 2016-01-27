package sune.ssp.logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import sune.ssp.util.Utils;

public abstract class ThreadLogger implements Logger {

	private Queue<String> logs = new ConcurrentLinkedQueue<>();
	private volatile boolean running;
	
	private Thread threadLogger;
	private Runnable runLogger = (() -> {
		while(running && !Thread.currentThread().isInterrupted()) {
			synchronized(logs) {
				if(!logs.isEmpty()) {
					String log;
					if((log = logs.poll()) != null) {
						printLog(log);
					}
				}
			}
			
			Utils.sleep(1);
		}
	});
	
	public abstract void printLog(String text);
	public void addLog(String text) {
		if(running) {
			synchronized(logs) {
				logs.add(text);
			}
		}
	}
	
	@Override
	public void init() {
		running		 = true;
		threadLogger = new Thread(runLogger);
		threadLogger.start();
	}
	
	@Override
	public void dispose() {
		while(!logs.isEmpty()) {
			Utils.sleep(1);
		}
		
		running = false;
		threadLogger.interrupt();
	}
	
	public void log(String string) {
		addLog(string);
	}
	
	public void logerr(String string) {
		addLog(string);
	}
	
	public void logerrf(String string, Object... args) {
		addLog(string);
	}
	
	public void logf(String string, Object... args) {
		addLog(string);
	}
}
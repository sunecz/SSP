package sune.ssp.logger;

public class QuietLogger implements Logger {
	
	private boolean init;
	
	@Override
	public void init() {
		init = true;
	}
	
	@Override
	public boolean isInitialized() {
		return init;
	}
	
	@Override public void dispose() {}
	@Override public void log(String string) {}
	@Override public void logf(String string, Object... args) {}
	@Override public void logerr(String string) {}
	@Override public void logerrf(String string, Object... args) {}
}
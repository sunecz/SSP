package sune.ssp.logger;

public interface Logger {
	
	public default void init() {
	}
	
	public default void dispose() {
	}
	
	public default void log(String string) {
		System.out.println(string);
	}
	
	public default void logf(String string, Object... args) {
		System.out.printf(string + "\n", args);
	}
	
	public default void logerr(String string) {
		System.err.println(string);
	}
	
	public default void logerrf(String string, Object... args) {
		System.err.printf(string + "\n", args);
	}
	
	public default boolean isInitialized() {
		return false;
	}
}
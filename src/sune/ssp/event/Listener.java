package sune.ssp.event;

@FunctionalInterface
public interface Listener<T> {
	
	public void call(T value);
}
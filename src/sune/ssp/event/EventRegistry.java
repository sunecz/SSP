package sune.ssp.event;

import java.util.List;

import sune.ssp.util.ListMap;

public class EventRegistry<T extends IEventType> {
	
	private final ListMap<EventType<T, ?>, Listener<?>>
		listeners = new ListMap<>();
	
	public <E> void add(EventType<T, E> type, Listener<E> listener) {
		if(type == null || listener == null) {
			throw new IllegalArgumentException(
				"Event type and listener cannot be null!");
		}
		listeners.ensure(type).add(listener);
	}
	
	public <E> void remove(EventType<T, E> type, Listener<E> listener) {
		if(type == null || listener == null) {
			throw new IllegalArgumentException(
				"Event type and listener cannot be null!");
		}
		listeners.ensure(type).remove(listener);
	}
	
	public <E> void call(EventType<T, E> type) {
		call0(type, null);
	}
	
	public <E> void call(EventType<T, E> type, E value) {
		call0(type, value);
	}
	
	@SuppressWarnings("unchecked")
	<E> void call0(EventType<T, E> type, E value) {
		if(type == null) {
			throw new IllegalArgumentException(
				"Event type cannot be null!");
		}
		if(listeners.has(type)) {
			List<Listener<?>> list = listeners.get(type);
			synchronized(list) {
				for(Listener<?> listener : list) {
					((Listener<E>) listener).call(value);
				}
			}
		}
	}
	
	public void clear() {
		listeners.clear();
	}
}
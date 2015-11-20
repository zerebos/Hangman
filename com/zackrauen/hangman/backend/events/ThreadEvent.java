package com.zackrauen.hangman.backend.events;

public class ThreadEvent {
	public static enum Event {start, stop};
	private Object source;
	private Event event;
	
	public ThreadEvent(Object s, Event e) {
		this.source=s;
		this.event=e;
	}
	
	public Object getSource() {
		return source;
	}
	
	public Event getEvent() {
		return event;
	}
}

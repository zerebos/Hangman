package com.zackrauen.hangman.backend.listeners;

import com.zackrauen.hangman.backend.events.ThreadEvent;

public interface ThreadListener {
	public void threadAction(ThreadEvent t);
}

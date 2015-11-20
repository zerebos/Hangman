package com.zackrauen.hangman.backend.listeners;

import com.zackrauen.hangman.backend.events.HangmanEvent;

public interface HangmanListener {
	public void gameEvent(HangmanEvent e);
}

package com.zackrauen.hangman.backend.events;

public class HangmanEvent {
	public static enum Event {NewMatch,NewGame,GivenUp,LetterGuessed,DuplicateGuess,NonDuplicateGuess,WrongGuess,
							CorrectGuess,GameLost,GameWon,GameQuit,MatchWin,MatchLoss,MatchTie}
	
	private Object source;
	private Event event;
	
	public HangmanEvent(Object s, Event e) {
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

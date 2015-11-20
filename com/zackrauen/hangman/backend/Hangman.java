package com.zackrauen.hangman.backend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.zackrauen.hangman.backend.events.HangmanEvent;
import com.zackrauen.hangman.backend.listeners.HangmanListener;

public class Hangman implements Serializable {
	
	public static enum Event {NewMatch,NewGame,GivenUp,LetterGuessed,DuplicateGuess,NonDuplicateGuess,WrongGuess,
		CorrectGuess,GameLost,GameWon,GameQuit,MatchWin,MatchLoss,MatchTie}
	
	private static final long serialVersionUID = 1L;

	List<HangmanListener> listeners = new ArrayList<HangmanListener>();
	
	private List<HangmanCharacter> gameChars = new ArrayList<HangmanCharacter>();
	private List<String> wordList = new ArrayList<String>();
	private List<HangmanCharacter> currentWord = new ArrayList<HangmanCharacter>();
	
	private Integer maxGuesses = 6;
	private Integer numOfWrongGuesses = 0;
	
	private Integer numOfWins = 0;
	private Integer numOfLosses = 0;
	
	private Boolean isGameOver = false;
	private Boolean isMatchOver = false;
	private Boolean isLastWord = false;
	
	private List<String> defaultWordList = new ArrayList<String>();
    
    private Random rand = new Random();
	
	public Hangman() { generateLetters(); }
	
	private void generateLetters() {
		gameChars.clear();
		for (int i='A';i<='Z';i++)
			gameChars.add(new HangmanCharacter((char) i));
		gameChars.add(new HangmanCharacter(' ',false));
		gameChars.add(new HangmanCharacter('/',false));
	}
	
	private void generateWordList(URL pathToFile) throws IOException, FileNotFoundException {
		wordList.clear();
	    String line;
	    //System.out.println(pathToFile);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(pathToFile.openStream()));
	    

	      while ((line = reader.readLine()) != null)
	    	  wordList.add(line);

	    reader.close();
	}
	
	public void setDefaultGame(URL pathToFile) throws IOException, FileNotFoundException {
		defaultWordList.clear();
	    String line;
	    BufferedReader reader = new BufferedReader(new InputStreamReader(pathToFile.openStream()));

	      while ((line = reader.readLine()) != null)
	    	  defaultWordList.add(line);

	    reader.close();
	}
	
	public void setupDefaultGame() {
		this.numOfLosses=0;
		this.numOfWins=0;
		this.numOfWrongGuesses=0;
		this.isGameOver=false;
		this.isMatchOver=false;
		this.isLastWord=false;
		this.generateLetters();
		wordList = defaultWordList;
		this.throwEvent(HangmanEvent.Event.NewMatch);
	}
	
	private void setCurrentWord(String word) {
		String convert = word.toUpperCase();
		currentWord.clear();
		for (int i=0;i<word.length();i++) {
			try {
				currentWord.add(gameChars.get(gameChars.indexOf(new HangmanCharacter(convert.charAt(i)))));	
			}
			catch (ArrayIndexOutOfBoundsException o) {
				currentWord.add(gameChars.get(gameChars.indexOf(new HangmanCharacter(convert.charAt(i),false))));
			}
			currentWord.get(i).setBeenGuessed(false);
		}
	}
	
	private void randomWordFromList() {
		Integer toGrab = rand.nextInt(wordList.size());
		setCurrentWord(wordList.get(toGrab));
		if (wordList.size()>1)
			wordList.remove(wordList.get(toGrab));
		else
			this.isLastWord=true;
	}
	
//	public void displayCurrentWord(Boolean displayOverride) {
//		for (int i=0;i<currentWord.size();i++) System.out.print(currentWord.get(i).displayValue(displayOverride) + " ");
//	}
//	
//	public void displayCurrentWord() {
//		for (int i=0;i<currentWord.size();i++) System.out.print(currentWord.get(i).displayValue() + " ");
//	}
	
	public String getWordAsString() {
		char[] word = new char[getCurrentWord().size()];
		for (int i=0;i<getCurrentWord().size();i++) {
			word[i] = getCurrentWord().get(i).getValue();
		}
		return String.valueOf(word);
	}
	
	public void guessLetter(char guess) throws Exception {
		if (!isGameOver) {
		this.throwEvent(HangmanEvent.Event.LetterGuessed);
		Integer index = gameChars.indexOf(new HangmanCharacter(Character.toUpperCase(guess)));
		Integer index2 = gameChars.indexOf(new HangmanCharacter(Character.toUpperCase(guess),true,true));
		
		if (index<0 && index2<0) throw new Exception("The guess was not a valid character");
		
		if (index>=0) {
			this.throwEvent(HangmanEvent.Event.NonDuplicateGuess);
			gameChars.get(index).setBeenGuessed(true);
			index = currentWord.indexOf(new HangmanCharacter(Character.toUpperCase(guess),true,true));
			if (index>=0) {
				this.throwEvent(HangmanEvent.Event.CorrectGuess);
				if (gameWon()) {
					numOfWins++;
					this.isGameOver = true;
					if (isLastWord)	matchOver();
					else this.throwEvent(HangmanEvent.Event.GameWon);
				}
			}
			else {
				numOfWrongGuesses++;
				this.throwEvent(HangmanEvent.Event.WrongGuess);
				if (numOfWrongGuesses.equals(maxGuesses)){
					numOfLosses++;
					this.isGameOver = true;
					if (isLastWord)	matchOver();
					else this.throwEvent(HangmanEvent.Event.GameLost);
					
				}
			}
		}
		else this.throwEvent(HangmanEvent.Event.DuplicateGuess);
		}	
	}
	
	private Boolean gameWon() {
		for (int i=0;i<currentWord.size();i++)
			if (!currentWord.get(i).hasBeenGuessed() && currentWord.get(i).isGuessable())	return false;
		return true;
	}
	
	private void matchOver() {
		isMatchOver=true;
		if (numOfWins>numOfLosses) throwEvent(HangmanEvent.Event.MatchWin);
		else if (numOfLosses>numOfWins) throwEvent(HangmanEvent.Event.MatchLoss);
		else throwEvent(HangmanEvent.Event.MatchTie);
	}
	
	private void throwEvent(HangmanEvent.Event e) {
	    for (HangmanListener l : listeners) l.gameEvent(new HangmanEvent(this,e));
	}
	
	public void setupNewMatch(URL pathToFile) throws IOException, FileNotFoundException {
		this.numOfLosses=0;
		this.numOfWins=0;
		this.numOfWrongGuesses=0;
		this.isGameOver=false;
		this.isMatchOver=false;
		this.isLastWord=false;
		this.generateLetters();
		this.generateWordList(pathToFile);
		this.throwEvent(HangmanEvent.Event.NewMatch);
	}
	
	public void addGameListener(HangmanListener toAdd) { listeners.add(toAdd); }
	
	public void removeListeners() { listeners.clear(); }
	
	public void setMaximumWrongGuesses(Integer i) { this.maxGuesses=i; }
	
	public List<HangmanCharacter> getCurrentWord() { return this.currentWord; }
	
	public Boolean isGameOver() { return this.isGameOver; }
	
	public List<HangmanCharacter> getGameChars() { return this.gameChars; }
	
	public Integer getNumOfLosses() { return this.numOfLosses; }
	
	public Integer getNumOfWins() { return this.numOfWins; }
	
	public Integer getNumOfWrongGuesses() { return this.numOfWrongGuesses; }
	
	public void quitGame() { this.throwEvent(HangmanEvent.Event.GameQuit); this.matchOver(); }
	
	public void newGame() {
		if (!isMatchOver) {
			this.isGameOver=false;
			this.numOfWrongGuesses=0;
			generateLetters();
			randomWordFromList();
			this.throwEvent(HangmanEvent.Event.NewGame);
		}
	}
	
	public void giveUp() {
		if (!isMatchOver) {
			this.isGameOver=true;
			this.numOfLosses++;
			this.throwEvent(HangmanEvent.Event.GivenUp);
			if (isLastWord)	matchOver();
		}
	}

}

package com.zackrauen.hangman.backend;

import gnu.io.SerialPort;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.zackrauen.hangman.backend.events.HangmanEvent;
import com.zackrauen.hangman.backend.events.ThreadEvent;
import com.zackrauen.hangman.backend.listeners.HangmanListener;
import com.zackrauen.hangman.backend.listeners.ThreadListener;

public class PeripheralControl implements Runnable, HangmanListener {
	
	List<ThreadListener> listeners = new ArrayList<ThreadListener>();
	
	public Integer shiftDelay = 150;
	public Integer messageDelay = 1500;
	
	private String lowLCDData = "0010";
	private String highLCDData = "0011";
	private String lowLCDCmd = "0000";
	private String highLCDCmd = "0001";
	
	private String lowLoadNum = "10000000";
	private String highLoadNum = "11000000";
	
	private String lowDecNum = "00000000";
	private String highDecNum = "01000000";
	
	private String defaultByte = "00000000";
	private String emptyByte = "00000000";
	
	private SerialPort comm = null;
	private char keyPressed;
	private Boolean interrupt = false;
	private HangmanEvent.Event toPerform;
	
	private Boolean newGame = false;
	private Boolean gameOver = false;
	private String longString = "";
	
	private Thread externalThread = new Thread();
	
	private Hangman game;
	
	public Dimension LCDSize = new Dimension(20,1);

	public PeripheralControl(SerialPort useThis, Hangman game) {
		this.game=game;
		this.game.addGameListener(this);
		comm=useThis;
		
		this.initializeLCD();
		this.initial("Welcome to Hangman! We'll start off on easy mode.");
		externalThread = new Thread(this);
		externalThread.start();
		
		try {
			comm.getOutputStream().write(Integer.parseUnsignedInt(defaultByte, 2));
			
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0000")),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
			
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("1100")),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("1100")),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("1100")),2));
		} catch (NumberFormatException | IOException e) {}
	}
	
	private void throwEvent(ThreadEvent e) {
	    for (ThreadListener l : listeners) l.threadAction(e);
	}
	
	public void addThreadListener(ThreadListener toAdd) { listeners.add(toAdd); }
	
	public void removeListeners() { listeners.clear(); }
	
	private void resetGuess() {
		try {
			//														LOAD | CLOCK | DONT CARE
				comm.getOutputStream().write(Integer.parseUnsignedInt(new String(lowLoadNum),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(new String(highLoadNum),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(new String(lowLoadNum),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(new String(defaultByte),2));
		} catch (IOException e) {

		}
	}
	
	private void wrongGuess() {
		try {
			//														LOAD | CLOCK | DONT CARE
				comm.getOutputStream().write(Integer.parseUnsignedInt(new String(lowDecNum),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(new String(highDecNum),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(new String(lowDecNum),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(new String(defaultByte),2));
		} catch (IOException e) {

		}
	}
	
	private void displayWord(List<HangmanCharacter> wordToDisplay, Boolean displayOverride) {
		
		clearDisplay();
		//initializeLCD();
	
		for (int i=0;i<wordToDisplay.size();i++) {
			if (wordToDisplay.get(i).hasBeenGuessed() || displayOverride || !wordToDisplay.get(i).isGuessable()) {
					sendLetterToSerialComm(wordToDisplay.get(i).getValue());
			}
			else {
					sendLetterToSerialComm('_');
			}
		}
//		try {
//			comm.getOutputStream().flush();
//		} catch (IOException e) {}
	}
	
	private void displayWord(String textToDisplay) {
		List<HangmanCharacter> wordToDisplay = new ArrayList<HangmanCharacter>();
	
		for (int i=0;i<textToDisplay.length();i++) {
			wordToDisplay.add(new HangmanCharacter(textToDisplay.charAt(i)));
		}
		displayWord(wordToDisplay, true);
	}
	
	private void updateWord(List<HangmanCharacter> word) {
		displayWord(word, false);
	}
	
	private void displayLong() {
		
		//initializeLCD();
		clearDisplay();

//		try {
//			//Thread.sleep(500);
//		comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat("1001"),2));
//		comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat("1001"),2));
//		comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat("1001"),2));
//		
//		comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat("0011"),2));
//		comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat("0011"),2));
//		comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat("0011"),2));
//		}
//		catch (IOException i) {}
	
		for (int i=0;i<longString.length();i++) {
			sendLetterToSerialComm(longString.charAt(i));
		}
		try {
			Thread.sleep(shiftDelay*3);
		} catch (InterruptedException e) {}

		for (int i=0;i<longString.length()-20;i++) {
			try {
				
				shiftLeft();
				Thread.sleep(shiftDelay);
			} catch (InterruptedException e) {}
		}
//		try {
//			comm.getOutputStream().flush();
//		} catch (IOException e) {}
	}
	
	private void newGameQuestion() {
		displayWord("New Game? (Y/N):");
		this.keyPressed=' ';
		toPerform=null;

		while(!interrupt) {
			//System.out.print("IN");
			try {
				keyPressed =(char) comm.getInputStream().read();
			} catch (IOException e) {}
			if (keyPressed=='Y') {
				toPerform=HangmanEvent.Event.NewGame;
				break;
			}
			if (keyPressed=='N') {
				toPerform=HangmanEvent.Event.GameQuit;
				break;
			}
		}
	}
	
	private void initialQuestion() {
		displayWord("New Match? (Y/N):");
		this.keyPressed=' ';
		toPerform=null;
		while(!interrupt) {
			//System.out.print("IN");
			try {
				keyPressed =(char) comm.getInputStream().read();
			} catch (IOException e) {}
			if (keyPressed=='Y') {
				toPerform=HangmanEvent.Event.NewMatch;
				break;
			}
			if (keyPressed=='N') {
				toPerform=HangmanEvent.Event.GameQuit;
				break;
			}
		}
	}
	
	private void gameOver() {
		displayWord("GAME OVER!");
	}
	
	private void newGame(List<HangmanCharacter> wordToDisplay) {
		resetGuess();
		displayWord(wordToDisplay, false);
	}
	
	private void longMessage(String longString, Boolean newGame, Boolean gameOver) {
		toPerform=null;
		this.longString=longString;
		this.newGame=newGame;
		this.gameOver=gameOver;
	}
	
	private void newMatch(String longString) {
		toPerform=HangmanEvent.Event.NewGame;
		this.longString=longString;
		this.newGame=false;
		this.gameOver=false;
	}
	
	private void initial(String longString) {
		toPerform=HangmanEvent.Event.NewMatch;
		this.longString=longString;
		this.newGame=true;
		this.gameOver=true;
	}
	
	public void interrupt() {
		this.interrupt=true;
		externalThread.interrupt();
		
		try {
			externalThread.join();
		} catch (InterruptedException e) {}
		
		this.clearDisplay();
		this.initializeLCD();
	}
	
	public void resetInterrupt() {
		this.interrupt=false;
		throwEvent(new ThreadEvent(this,ThreadEvent.Event.stop));
	}


	@Override
	public void run() {
		throwEvent(new ThreadEvent(this,ThreadEvent.Event.start));
		clearDisplay();
		

		displayLong();
		
		if (!Thread.currentThread().isInterrupted()) {
			try {
				if (newGame && gameOver) {
					Thread.sleep(messageDelay);
					initialQuestion();
				}
				else {
					if (newGame) {
						Thread.sleep(messageDelay);
						newGameQuestion();
					}
					else if (gameOver) {
						Thread.sleep(messageDelay*2);
						gameOver();
					}
					else {
						Thread.sleep(messageDelay);
					}
				}
			} catch (InterruptedException e) {
			}
		}
			
			
			if (toPerform!=null) {
				if (toPerform==HangmanEvent.Event.NewGame)
					game.newGame();
				else if (toPerform==HangmanEvent.Event.GameQuit)
					game.quitGame();
				else if (toPerform==HangmanEvent.Event.NewMatch)
						game.setupDefaultGame();
			}
			System.out.println("DONE");
		throwEvent(new ThreadEvent(this,ThreadEvent.Event.stop));
		return;
		
	}

	@Override
	public void gameEvent(HangmanEvent e) {
		
		float[] hsbvals = new float[3];
		Color.RGBtoHSB(46, 110, 0, hsbvals);
		
		if (e.getEvent().equals(HangmanEvent.Event.CorrectGuess) || e.getEvent().equals(HangmanEvent.Event.WrongGuess)) {
			if (e.getEvent().equals(HangmanEvent.Event.WrongGuess)) {
				wrongGuess();
			}
			else {
				updateWord(game.getCurrentWord());
			}
		}
		
		if (e.getEvent().equals(HangmanEvent.Event.GameLost)) {
			longMessage("Sorry! The correct word was " + game.getWordAsString() + " you have solved " + game.getNumOfWins() + " puzzle(s) out of " + (game.getNumOfWins()+game.getNumOfLosses()),true,false);
			externalThread = (new Thread(this));
			externalThread.start();
		}
		
		if (e.getEvent().equals(HangmanEvent.Event.NewMatch) || e.getEvent().equals(HangmanEvent.Event.NewGame)) {
			if (e.getEvent().equals(HangmanEvent.Event.NewMatch)) {
					this.newMatch("Well let's get started, shall we?");
					externalThread = new Thread(this);
					externalThread.start();
			}
			else if (e.getEvent().equals(HangmanEvent.Event.NewGame)) {
				this.newGame(game.getCurrentWord());
			}
		}
		
		if (e.getEvent().equals(HangmanEvent.Event.GameWon)) {
			this.longMessage("Well done! You have solved " + game.getNumOfWins() + " puzzle(s) out of " + (game.getNumOfWins()+game.getNumOfLosses()),true,false);
			externalThread = (new Thread(this));
			externalThread.start();
		}
		
		if (e.getEvent().equals(HangmanEvent.Event.MatchWin)) {
			String congratsText = "Good job, you got " + game.getNumOfWins() + " out of " + (game.getNumOfWins()+game.getNumOfLosses());
			this.longMessage(congratsText, false, true);
			externalThread = (new Thread(this));
			externalThread.start();
		}
		else if (e.getEvent().equals(HangmanEvent.Event.MatchLoss)) {
			String lossMessage = "Nice try, you only got " + game.getNumOfWins() + " out of " + (game.getNumOfWins()+game.getNumOfLosses());
			this.longMessage(lossMessage, false, true);
			externalThread = (new Thread(this));
			externalThread.start();
		}
		else if (e.getEvent().equals(HangmanEvent.Event.MatchTie)) {
			String tieMessage = "A tie, not bad, you got " + game.getNumOfWins() + " out of " + (game.getNumOfWins()+game.getNumOfLosses());
			this.longMessage(tieMessage, false, true);
			externalThread = (new Thread(this));
			externalThread.start();
		}
		
	}
	
	
	private void clearDisplay() {
		try {
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat("0000"),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat("0000"),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat("0000"),2));
			
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat("0001"),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat("0001"),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat("0001"),2));
		} catch (NumberFormatException | IOException e1) {}
	}
	
	private void sendLetterToSerialComm(char c) {
		
		Character theChar = new Character(c);
		String toSend;
		String lowerNibble;
		String upperNibble;
			try {	
				toSend = emptyByte.substring(0, 8-Integer.toBinaryString(theChar.hashCode()).length()) + (Integer.toBinaryString(theChar.hashCode()));
				lowerNibble = new String(toSend.substring(toSend.length()-4, toSend.length()));
				upperNibble = new String(toSend.substring(0, 4));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDData.concat(upperNibble),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDData.concat(upperNibble),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDData.concat(upperNibble),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDData.concat(lowerNibble),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDData.concat(lowerNibble),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDData.concat(lowerNibble),2));

			} catch (IOException e) {
				
			}
	}
	
// CANNOT BE USED FOR LONG MESSAGES
//	public void sendLetterToSerialComm(Integer x, Integer y, char c) throws Exception {
//		
//		if (x>LCDSize.width || y >LCDSize.height)
//			throw new Exception("The location specified does not exist.");
//		
//		Character theChar = new Character(c);
//		String toSend = new String("1").concat(emptyByte.substring(0, 7-Integer.toBinaryString(x-1).length()).concat(Integer.toBinaryString(x-1)));
//		String lowerNibble = new String(toSend.substring(toSend.length()-4, 8));
//		String upperNibble = new String(toSend.substring(0, 4));
//		
//
//
//			try {
//				
//				// position
//				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(upperNibble),2));
//				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(upperNibble),2));
//				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(upperNibble),2));
//				
//				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(lowerNibble),2));
//				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(lowerNibble),2));
//				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(lowerNibble),2));
//				
//				
//				comm.getOutputStream().write(Integer.parseUnsignedInt(defaultByte,2)); // Clear the stream
//				comm.getOutputStream().write(Integer.parseUnsignedInt(defaultByte,2)); // Clear the stream
//				
//				// data
//				toSend = emptyByte.substring(0, 8-Integer.toBinaryString(theChar.hashCode()).length()).concat(Integer.toBinaryString(theChar.hashCode()));
//				lowerNibble = new String(toSend.substring(toSend.length()-4, toSend.length()));
//				upperNibble = new String(toSend.substring(0, 4));
//				
//				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDData.concat(upperNibble),2));
//				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDData.concat(upperNibble),2));
//				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDData.concat(upperNibble),2));
//				
//				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDData.concat(lowerNibble),2));
//				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDData.concat(lowerNibble),2));
//				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDData.concat(lowerNibble),2));
//				
//
//			} catch (IOException e) {
//				
//			}
//	}
	
	public void initializeLCD() {
		
			try {
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0011")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0011")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0011")),2));
				
				Thread.sleep(5);
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0011")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0011")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0011")),2));
				
				Thread.sleep(1);
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0011")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0011")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0011")),2));
				
				Thread.sleep(1);
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0010")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0010")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0010")),2));

				Thread.sleep(1);
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0010")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0010")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0010")),2));
				
				Thread.sleep(5);
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("1000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("1000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("1000")),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0001")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0001")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0001")),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0110")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0110")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0110")),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0000")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0000")),2));
				
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("1100")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("1100")),2));
				comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("1100")),2));
				
			
			} catch (IOException | InterruptedException e) {

			}
	}
	
	private void shiftLeft() {
			try {
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0001")),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("0001")),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("0001")),2));
			
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("1000")),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(highLCDCmd.concat(new String("1000")),2));
			comm.getOutputStream().write(Integer.parseUnsignedInt(lowLCDCmd.concat(new String("1000")),2));
			}
			catch (IOException i) {
				
			}
	}

}

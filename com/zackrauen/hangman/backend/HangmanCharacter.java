package com.zackrauen.hangman.backend;

public class HangmanCharacter {
	private Character value;
	private Boolean hasBeenGuessed = false;
	private Boolean isGuessable = true;

	public HangmanCharacter(char value) { this.setValue(value); }
	
	public HangmanCharacter(char value, Boolean valid) {
		this.setValue(value);
		this.setGuessable(valid);
	}
	
	public HangmanCharacter(char value, Boolean beenGuessed, Boolean valid) {
		this.setValue(value);
		this.setBeenGuessed(beenGuessed);
		this.setGuessable(valid);
	}
	
	public char displayValue() {
		if (this.hasBeenGuessed() || !this.isGuessable())
			return value.charValue();
		else
			return '_';
	}
	
	public char displayValue(Boolean forceDisplay) {
		if (forceDisplay)
			return value.charValue();
		else
			return displayValue();
	}
	
	public char getValue() { return value.charValue(); }

	public void setValue(char value) { this.value = new Character(value); }
	
	public void setValue(Character value) { this.value = value; }

	public Boolean hasBeenGuessed() { return hasBeenGuessed; }

	public void setBeenGuessed(Boolean hasBeenGuessed) { this.hasBeenGuessed = hasBeenGuessed; }

	public Boolean isGuessable() { return isGuessable; }

	public void setGuessable(Boolean isValid) { this.isGuessable = isValid; }

	public Boolean equals(HangmanCharacter o) {
		if (o.getValue() == this.getValue() && o.hasBeenGuessed()==this.hasBeenGuessed() && o.isGuessable() == this.isGuessable())
			return true;
		else
			return false;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	    return equals((HangmanCharacter) obj);
	}

}

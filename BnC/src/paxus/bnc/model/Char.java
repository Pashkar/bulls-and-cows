package paxus.bnc.model;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;


public class Char {
	 
	static final char NULL_CHAR = '?';

	public final char ch;
	
	public final String asString;
	
	public final Alphabet alphabet;
	
	public Char(char ch, Alphabet alphabet) throws BncException {
		if (alphabet == null && ch != NULL_CHAR)
			throw new NullPointerException("Alphabet must not be null");
		if (alphabet != null && !alphabet.isCharValid(ch))
			throw new BncException("\"" + ch + "\" isn't allowed in alphabet \"" + alphabet.getName() + "\"");
		this.alphabet = alphabet;
		this.ch = ch;
		this.asString = ch + "";
	}

	public ENCharState getState() {
		return alphabet.getStateForChar(ch);
	}

	//package-private
	//change using Run - it cares of consistancy 
	ENCharState moveState(ICharStateSequencer css, ENCharState... forbidden) {
		return alphabet.moveCharState(this, css, forbidden);
	}
	
	@Override
	public String toString() {
		return asString + getState();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((alphabet == null) ? 0 : alphabet.hashCode());
		result = prime * result + ch;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Char other = (Char) obj;
		if (alphabet == null) {
			if (other.alphabet != null)
				return false;
		} else if (!alphabet.equals(other.alphabet))
			return false;
		if (ch != other.ch)
			return false;
		return true;
	}

	public static final Char NULL;
	static {
		Char ch = null;
		try {
			ch = new Char(NULL_CHAR, null) {
				ENCharState moveState(ICharStateSequencer css, ENCharState... forbidden) {
					throw new UnsupportedOperationException();
				}
				@Override
				public ENCharState getState() {
					return ENCharState.NONE;
				}
			};
		} catch (BncException e) {}
		NULL = ch;
	}
}

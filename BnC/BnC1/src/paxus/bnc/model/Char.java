package paxus.bnc.model;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;


public final class Char {
	 
	public final Character ch;
	
	public final Alphabet alphabet;
	
	public Char(Character ch, Alphabet alphabet) throws BncException {
		if (!alphabet.isCharValid(ch))
			throw new BncException("\"" + ch + "\" isn't allowed in alphabet \"" + alphabet.getName() + "\"");
		this.ch = ch;
		this.alphabet = alphabet;
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
		return "" + ch + getState();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((alphabet == null) ? 0 : alphabet.hashCode());
		result = prime * result + ((ch == null) ? 0 : ch.hashCode());
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
		if (ch == null) {
			if (other.ch != null)
				return false;
		} else if (!ch.equals(other.ch))
			return false;
		return true;
	}
}

package paxus.bnc.model;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateChangedListener;

/*
 * Would be better to have Char as a nested class for Alphabet. 
 * While serializing, there must be an Alphabet instance which cares of Char instances.  
*/
public class Char implements Serializable, Comparable<Char> {
	 
	static final char NULL_CHAR = ' ';

	public Character ch;	//not-final due to readObject()
	
	public final String asString;
	
	public final Alphabet alphabet;
	
	private transient final ArrayList<ICharStateChangedListener> stateChangedListenerList = new ArrayList<ICharStateChangedListener>();
	
	//package-private
	//for init from alphabet only - no checks needed
	Char(Alphabet alphabet, char ch) {
		this.alphabet = alphabet;
		this.ch = ch;
		this.asString = ch + "";
	}
	
	private Object readResolve() throws ObjectStreamException {
		if (Run.alphabet == null)
			throw new NullPointerException("Run.alphabet must not be null");
		return Run.alphabet.getCharInstance(ch);
	}
	
	private void readObject(java.io.ObjectInputStream stream) throws IOException {
		this.ch = stream.readChar();
	}
	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeChar(ch);
	}
	
	public static Char valueOf(Character ch, Alphabet alphabet) throws BncException {
		if (alphabet == null) {
			if (ch == NULL_CHAR)
				return NULL;
			else
				throw new NullPointerException("Alphabet must not be null"); 
		}
		if (ch == Char.NULL_CHAR)
			return Char.NULL;
		if (!alphabet.isValidSymbol(ch))
			throw new BncException("Symbol " + ch + " is not allowed in alphabet " + alphabet);
		return alphabet.char2char.get(ch);	//all char must have been initialized on alphabet load
	}

/*	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		if (Run.alphabet == null)
			throw new NullPointerException("Run.alphabet must not be null");
		
		this.alphabet = alphabet;
		this.ch = ch;
		this.asString = ch + "";
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeChar(ch);
	}*/
	

	public ENCharState getState() {
		return alphabet.char2state.get(ch);
	}

	//change using Run - it cares of consistency 
	public ENCharState moveState(ENCharState... forbidden) throws BncException {
		return alphabet.moveCharState(ch, forbidden);
	}
	
	/**
	 * Subscribe to stateChanged notifications for exact Char instance.
	 * {@link #onStateChanged(Character, ENCharState)} will be invoked with Character argument from this instance.
	 * Use {@link Alphabet#addAllCharsStateChangedListener(ICharStateChangedListener)} to subscribe to all Char's state updates.
	 */
	public void addStateChangedListener(ICharStateChangedListener listener) {
		stateChangedListenerList.add(listener);
	}
	
	public void removeStateChangedListener(ICharStateChangedListener listener) {
		stateChangedListenerList.remove(listener);
	}
	
	public void onStateChanged(Character ch, ENCharState newState) throws BncException {
		for (ICharStateChangedListener listener : stateChangedListenerList)
			listener.onCharStateChanged(ch, newState);
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
	
	public int compareTo(Char obj) {
		if (this == obj)
			return 0;
		if (obj == null)
			throw new NullPointerException();
		Char other = (Char) obj;
		return this.ch.charValue() - other.ch.charValue();
	}

	public static final Char NULL;
	static {
		Char ch = null;
		ch = new Char(null, NULL_CHAR) {
			private ENCharState state = ENCharState.NONE; 
			@Override
			public ENCharState moveState(ENCharState... forbidden) {
				return state;
			}
			@Override
			public ENCharState getState() {
				return state;
			}
			@Override
			public void addStateChangedListener(ICharStateChangedListener listener) {
			}
			@Override
			public void removeStateChangedListener(ICharStateChangedListener listener) {
			}
		};
		NULL = ch;
	}
}

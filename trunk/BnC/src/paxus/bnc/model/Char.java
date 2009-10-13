package paxus.bnc.model;

import java.util.ArrayList;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.IStateChangedListener;


public class Char {
	 
	static final char NULL_CHAR = '?';

	public final Character ch;
	
	public final String asString;
	
	public final Alphabet alphabet;
	
	//List - since there is only one Char instance for several CharView instances
	private final ArrayList<IStateChangedListener> stateChangedListenerList = new ArrayList<IStateChangedListener>();


	/*//List - since there is only one Char instance for several CharView instances
	private ArrayList<IStateChangedListener> stateChangedListenerList = new ArrayList<IStateChangedListener>();
	*/
/*	private Char(char ch, Alphabet alphabet) throws BncException {
		if (alphabet == null && ch != NULL_CHAR)
			throw new NullPointerException("Alphabet must not be null");
		if (alphabet != null && !alphabet.isCharValid(ch))
			throw new BncException("\"" + ch + "\" isn't allowed in alphabet \"" + alphabet.getName() + "\"");
		this.alphabet = alphabet;
		this.ch = ch;
		this.asString = ch + "";
	}*/
	
	//package-private
	//for init from alphabet only - no checks needed
	Char(Alphabet alphabet, char ch) {
		this.alphabet = alphabet;
		this.ch = ch;
		this.asString = ch + "";
	}
	
	public static Char valueOf(Character ch, Alphabet alphabet) throws BncException {
		if (alphabet == null) {
			if (ch == NULL_CHAR)
				return NO_ALPHA;
			else
				throw new NullPointerException("Alphabet must not be null"); 
		}
		if (!alphabet.isValidSymbol(ch))
			throw new BncException("Symbol " + ch + " is not allowed in alphabet " + alphabet);
		return alphabet.char2char.get(ch);	//all char must be initialized on alphabet load
	}

	public ENCharState getState() {
		return alphabet.char2state.get(ch);
	}

	//change using Run - it cares of consistency 
	public ENCharState moveState(ENCharState... forbidden) {
		return alphabet.moveCharState(ch, forbidden);
	}
	
	public void addStateChangedListener(IStateChangedListener listener) {
		stateChangedListenerList.add(listener);
	}
	
	public void removeStateChangedListener(IStateChangedListener listener) {
		stateChangedListenerList.remove(listener);
	}
	
	public void onStateChanged(Character ch, ENCharState newState) {
		for (IStateChangedListener listener : stateChangedListenerList)
			listener.onStateChanged(ch, newState);
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

	public static final Char NO_ALPHA;
	static {
		Char ch = null;
		ch = new Char(null, NULL_CHAR) {
			private ENCharState state = ENCharState.NONE; 
			@Override
			public ENCharState moveState(ENCharState... forbidden) {
				state = ICharStateSequencer.FORWARD.nextState(state, null, -1, forbidden);
				return state;
			}
			@Override
			public ENCharState getState() {
				return state;
			}
			@Override
			public void addStateChangedListener(IStateChangedListener listener) {
			}
			@Override
			public void removeStateChangedListener(IStateChangedListener listener) {
			}
		};
		NO_ALPHA = ch;
	}
}

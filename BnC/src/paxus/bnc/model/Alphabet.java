package paxus.bnc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.IStateChangedListener;
import paxus.bnc.controller.IStatesCounter;

public abstract class Alphabet implements IStatesCounter {
	
	private final HashSet<Character> symbols = new HashSet<Character>();
	
	final HashMap<Character, ENCharState> char2state = new HashMap<Character, ENCharState>();
	
	final HashMap<Character, Char> char2char = new HashMap<Character, Char>();
	
	//List - since there is only one Char instance for several CharView instances
	private final ArrayList<IStateChangedListener> stateChangedListenerList = new ArrayList<IStateChangedListener>();
	
	private ICharStateSequencer css = ICharStateSequencer.FORWARD;
	public void setCss(ICharStateSequencer defaultCss) {
		this.css = defaultCss;
	}

	private int presentStateCount; 
	public int getStatesCount(ENCharState state, Char ch, int pos) {
		if (state != ENCharState.PRESENT)
			return -1;
		return presentStateCount;
	}

	public Alphabet() {
		//replace members by local references (for-loop increases impact) 
		final HashMap<Character, ENCharState> char2state2 = char2state;
		final HashMap<Character, Char> char2char2 = char2char;
		final HashSet<Character> ss = symbols;
		
		char[] charArray = getSymbolsLine().toCharArray();
		for (int i = 0; i < charArray.length; i++) { 
			Character character = new Character(charArray[i]);
			ss.add(character);	//init list of allowed symbols 
			char2state2.put(character, ENCharState.NONE);	//init states for all chars
			char2char2.put(character, new Char(this, charArray[i])); //init all Char objects for all allowed symbols
		}
	}
	
	/**
	 * 	Clear all chars states. Clears defaultCss.
	 */
	public void clear() {
		final HashMap<Character, ENCharState> char2state2 = char2state;
		for (Character ch : symbols) {
			char2state2.put(ch, ENCharState.NONE);	//clear states for all chars
		}
		presentStateCount = 0;
		css = null;
		stateChangedListenerList.clear();
	}
	
	public Collection<Char> getAllChars() {
		return char2char.values();
	}

	//package-private
	//change using Run - it cares of consistency
	//change state using predefined CharStateSequencer css
	final ENCharState moveCharState(Character ch, ENCharState... forbidden) {
		return setCharState(ch, css.nextState(char2state.get(ch), null, -1, forbidden));
	}

	private ENCharState setCharState(Character ch, ENCharState newState) {
		ENCharState curState = char2state.get(ch);
		if (newState == curState)
			return newState;
		
		if (curState == ENCharState.PRESENT)	
			presentStateCount--;
		else if (newState == ENCharState.PRESENT) 
			presentStateCount++;

		char2state.put(ch, newState);
		notifyStateChangedListeners(ch, newState);
		return newState;
	}
	
	public void addStateChangedListener(IStateChangedListener listener) {
		this.stateChangedListenerList.add(listener);
	}
	
	public void removeStateChangedListener(IStateChangedListener listener) {
		this.stateChangedListenerList.remove(listener);
	}
	
	private void notifyStateChangedListeners(Character ch, ENCharState newState) {
		for (IStateChangedListener listener : stateChangedListenerList) 
			listener.onStateChanged(ch, newState);
	}

	public final boolean isValidSymbol(Character ch) {
		return symbols.contains(ch);
	}
	
	protected abstract String getSymbolsLine();

	public abstract String getName();
	
	@Override
	public String toString() {
		return getName() + "";
	}
	
	public Char getCharInstance(char ch) {
		return char2char.get(ch);
	}

	public static class Latin extends Alphabet {
		@Override
		public String getName() {
			return "Latin";
		}
		@Override
		protected String getSymbolsLine() {
			return "abcdefghijklmnopqrstuvwxyz";
		}
	};
	
	public static class Digital extends Alphabet {
		@Override
		public String getName() {
			return "Digital";
		}
		@Override
		protected String getSymbolsLine() {
			return "0123456789";
		}
	};
}

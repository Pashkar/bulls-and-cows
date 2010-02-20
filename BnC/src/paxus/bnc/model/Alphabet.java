package paxus.bnc.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateChangedListener;
import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.IStatesCounter;

public abstract class Alphabet implements Externalizable, IStatesCounter {
	public static final String LATIN = "Latin";
	public static final String DIGITAL = "Digital";	
	
	private final HashSet<Character> symbols = new HashSet<Character>();
	
	//TODO Optimization : replace map with ordinal array by char->int
	final HashMap<Character, ENCharState> char2state = new HashMap<Character, ENCharState>();
	
	//TODO Optimization : replace map with ordinal array by char->int
	transient final HashMap<Character, Char> char2char = new HashMap<Character, Char>();
	
	private final ArrayList<ICharStateChangedListener> stateChangedListenerList = new ArrayList<ICharStateChangedListener>();
	
	private ICharStateSequencer css = ICharStateSequencer.FORWARD;
	public void setCss(ICharStateSequencer defaultCss) {
		this.css = defaultCss;
	}

	private int presentStateCount; 
	public int getStatesCount(ENCharState state, Character ch, int pos) {
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
	 * Use {@link Char#addStateChangedListener(ICharStateChangedListener)} to subscribe to exact Char instance notifications.
	 */
	public void addAllCharsStateChangedListener(ICharStateChangedListener listener) {
		stateChangedListenerList.add(listener);
	}
	
	public void removeAllCharsStateCRhangedListener(ICharStateChangedListener listener) {
		stateChangedListenerList.remove(listener);
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
	
	public Collection<Char> getAllCharsSorted() {
		List<Char> values = new ArrayList<Char>(char2char.values());
		Collections.sort(values);
		return values;
	}

	//package-private
	//change using Run - it cares of consistency
	//change state using predefined CharStateSequencer css
	final ENCharState moveCharState(Character ch, ENCharState... forbidden) throws BncException {
		return setCharState(ch, css.nextState(char2state.get(ch), null, -1, forbidden));
	}

	private ENCharState setCharState(Character ch, ENCharState newState) throws BncException {
		final HashMap<Character, ENCharState> char2state2 = char2state;
		
		ENCharState curState = char2state2.get(ch);
		if (newState == curState)
			return newState;
		
		if (curState == ENCharState.PRESENT)	
			presentStateCount--;
		else if (newState == ENCharState.PRESENT) 
			presentStateCount++;

		char2state2.put(ch, newState);
		char2char.get(ch).onStateChanged(ch, newState);		//notify exact Char
		notifyCharStateChangedListeners(ch, newState);		//notify all listeners, they will filter by Char in args 
		return newState;
	}
	
	//TODO extract new Thread
	private void notifyCharStateChangedListeners(Character ch, ENCharState newState) throws BncException {
		for (ICharStateChangedListener listener : stateChangedListenerList)
			listener.onCharStateChanged(ch, newState);
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

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		final HashMap<Character, ENCharState> char2state2 = char2state;
		for (Character ch : char2state2.keySet())
			char2state2.put(ch, (ENCharState) in.readObject());
		presentStateCount = in.readInt();
		css = (ICharStateSequencer) in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		final Collection<ENCharState> states = char2state.values();
		for (ENCharState state : states) 
			out.writeObject(state);
		out.writeInt(presentStateCount);
		out.writeObject(css);
	}

	public static class Latin extends Alphabet {
		@Override
		public String getName() {
			return LATIN;
		}
		@Override
		protected String getSymbolsLine() {
			return "abcdefghijklmnopqrstuvwxyz";
		}
	};
	
	public static class Digital extends Alphabet {
		@Override
		public String getName() {
			return DIGITAL;
		}
		@Override
		protected String getSymbolsLine() {
			return "0123456789";
		}
	}
}

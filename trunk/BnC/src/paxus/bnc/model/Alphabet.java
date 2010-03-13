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
	public static final int 		DIGITAL_ID = 0;
	public static final String 		DIGITAL_NAME = "Digital";	
	public static final int 		DIGITAL_MIN_WORD_LENGTH = 3;

	public static final int 		LATIN_ID = 1;
	public static final String 		LATIN_NAME = "Latin";
	public static final int 		LATIN_MIN_WORD_LENGTH = 4;
	
	public static final int 		CYRILLIC_ID = 2;
	public static final String 		CYRILLIC_NAME = "Cyrillic";
	public static final int 		CYRILLIC_MIN_WORD_LENGTH = 4;
	
	private final HashSet<Character> symbols = new HashSet<Character>();
	private ICharStateSequencer css = ICharStateSequencer.FORWARD;
	private int presentStateCount;
	
	//TODO Optimization : replace map with ordinal array by char->int
	final HashMap<Character, ENCharState> char2state = new HashMap<Character, ENCharState>();
	transient final HashMap<Character, Char> char2char = new HashMap<Character, Char>();
	
	private final ArrayList<ICharStateChangedListener> stateChangedListenerList = new ArrayList<ICharStateChangedListener>();

	public static int getMinSize(int id) {
		switch (id) {
			case DIGITAL_ID:
				return DIGITAL_MIN_WORD_LENGTH;
			case LATIN_ID:
				return LATIN_MIN_WORD_LENGTH;
			case CYRILLIC_ID:
				return CYRILLIC_MIN_WORD_LENGTH;
			default:
				return -1;
		}
	}
	
	public abstract int getId();
	public abstract String getName();
	protected abstract String getSymbolsLine();
	
	
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
	
	public void setCss(ICharStateSequencer defaultCss) {
		this.css = defaultCss;
	}
	 
	public int getStatesCount(ENCharState state, Character ch, int pos) {
		if (state != ENCharState.PRESENT)
			return -1;
		return presentStateCount;
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
	
	public final List<Character> getSymbols() {
		return new ArrayList<Character>(symbols);	//return copy, not the instance
	}
	
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

	public final static class Cyrrilic extends Alphabet {
		@Override
		public int getId() {
			return Alphabet.CYRILLIC_ID;
		}
		@Override
		public String getName() {
			return CYRILLIC_NAME;
		}
		@Override
		protected String getSymbolsLine() {
			return "אבגדהוזחטיךכלםמןנסעףפץצקרשתûü‎‏ÿ";
		}
	};
	
	public final static class Latin extends Alphabet {
		@Override
		public int getId() {
			return Alphabet.LATIN_ID;
		}
		@Override
		public String getName() {
			return LATIN_NAME;
		}
		@Override
		protected String getSymbolsLine() {
			return "abcdefghijklmnopqrstuvwxyz";
		}
	};
	
	public final static class Digital extends Alphabet {
		@Override
		public int getId() {
			return Alphabet.DIGITAL_ID;
		}
		@Override
		public String getName() {
			return DIGITAL_NAME;
		}
		@Override
		protected String getSymbolsLine() {
			return "0123456789";
		}
	}
}

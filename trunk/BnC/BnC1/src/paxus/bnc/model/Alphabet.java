package paxus.bnc.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;

public abstract class Alphabet implements IStatesCounter {
	
	private final Set<Character> symbols;
	
	private final Map<Character, ENCharState> char2state = new HashMap<Character, ENCharState>();
	
	private ICharStateSequencer defaultCss;
	/**
	 * Set up default CharStateSequencer
	 */
	public void setDefaultCss(ICharStateSequencer defaultCss) {
		this.defaultCss = defaultCss;
	}

	private int presentStateCount; 
	@Override
	public int getStatesCount(ENCharState state) {
		if (state != ENCharState.PRESENT)
			return -1;
		return presentStateCount;
	}

	private Alphabet() {
		symbols = init();
		reinit();
	}
	
	private Set<Character> init() {
		Set<Character> ss = new HashSet<Character>();
		for (char ch : getSymbolsLine().toCharArray()) 
			ss.add(Character.valueOf(ch));
		return Collections.unmodifiableSet(ss);
	}
	
	/**
	 * 	Clear all chars states. Clears defaultCss.
	 */
	public void reinit() {
		for (Character ch : symbols) {
			char2state.put(ch, ENCharState.NONE);	//no null state for entire alphabet
		}
		presentStateCount = 0;
		defaultCss = null;
	}

	public final ENCharState getStateForChar(Character ch) {
		return char2state.get(ch);
	}
	
	//package-private
	//change using Run - it cares of consistancy
	final ENCharState moveCharState(Character ch, ENCharState... forbidden) throws BncException {
		return moveCharState(new Char(ch, this), defaultCss, forbidden);
	}
	
	//package-private
	//change using Run - it cares of consistancy
	final ENCharState moveCharState(Character ch, ICharStateSequencer css, ENCharState... forbidden) throws BncException {
		return moveCharState(new Char(ch, this), css, forbidden);
	}

	//package-private
	//change using Run - it cares of consistancy
	final ENCharState moveCharState(Char ch, ICharStateSequencer css, ENCharState... forbidden) {
		ICharStateSequencer newCss = css != null ? css : defaultCss; 
		return setCharState(ch.ch, newCss.nextState(char2state.get(ch.ch), forbidden));
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
		return newState;
	}
	
	
	public final boolean isCharValid(Character ch) {
		return symbols.contains(ch);
	}
	
	protected abstract String getSymbolsLine();

	public abstract String getName();
	
	@Override
	public String toString() {
		return char2state + "";
	}

	public static final Alphabet LATIN = new Alphabet() {
		@Override
		public String getName() {
			return "Latin";
		}
		@Override
		protected String getSymbolsLine() {
			return "abcdefghijklmnopqrstuvwxyz";
		}
		
	};
	
	public static final Alphabet CYRILLIC = new Alphabet() {
		@Override
		public String getName() {
			return "Cyrillic";
		}
		@Override
		protected String getSymbolsLine() {
			return "אבגדהוזחטיךכלםמןנסעףפץצקרשתûü‎‏ÿ";
		}
	};
	
	public static final Alphabet DIGITAL = new Alphabet() {
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

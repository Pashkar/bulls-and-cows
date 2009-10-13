package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.IStateChangedListener;

public class CharTest extends TestCase {

	public void testNull() throws BncException {
		Char ch = Char.NO_ALPHA;
		
		assertEquals(new Character('?'), ch.ch);
		assertEquals("?", ch.asString);
		
		assertEquals(ENCharState.ABSENT, ch.moveState());
		assertEquals(ENCharState.PRESENT, ch.moveState());
		assertEquals(ENCharState.NONE, ch.moveState());

		assertEquals(ENCharState.NONE, ch.getState());
		
		//try null alphabet
		boolean ok = false;
		try {Char.valueOf('a', null); } 
		catch (NullPointerException e) {ok = true;}
		if (!ok)
			throw new BncException();
	}
	
	public void testValueOf() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		Char ch = Char.valueOf('a', la);
		assertTrue(ch == la.getCharInstance('a'));
		
		assertNotNull(la.getCharInstance('b'));	//not instantiated explicitly, is actually created by alphabet constructor
		assertTrue(Char.NO_ALPHA == Char.valueOf(Char.NULL_CHAR, null));
	}
	
	public void testWrongAlphabet() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		Char.valueOf('a', la);
		
		final Alphabet da = new Alphabet.Digital();
		Char.valueOf('1', da);
		
		boolean ok = false;
		try {Char.valueOf('z', da); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
	}
	
	public void testStateChangeListener() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		Char chA = Char.valueOf('a', la);
		Char chB = Char.valueOf('b', la);
		final int[] counterA = {0};
		final IStateChangedListener listenerA = new IStateChangedListener() {
			public void onStateChanged(Character ch, ENCharState newState) {
				counterA[0]++;
			}
		};
		final int[] counterB = {0};
		final IStateChangedListener listenerB = new IStateChangedListener() {
			public void onStateChanged(Character ch, ENCharState newState) {
				counterB[0]++;
			}
		};
		
		//no listeners 
		chA.moveState();
		assertEquals(0, counterA[0]);

		chA.addStateChangedListener(listenerA);
		chB.addStateChangedListener(listenerB);
		
		//change by Char
		chA.moveState();
		assertEquals(1, counterA[0]);

		//change by alphabet
		la.moveCharState(chA.ch);
		assertEquals(2, counterA[0]);
		
		//no allowed states - should not change state and notify listeners
		chA.moveState(ENCharState.NONE, ENCharState.ABSENT, ENCharState.PRESENT);
		assertEquals(2, counterA[0]);

		//another char, another listener
		chB.moveState();
		assertEquals(2, counterA[0]);
		assertEquals(1, counterB[0]);
		
		//two listeners, both notified
		chA.addStateChangedListener(listenerB);
		chA.moveState();
		assertEquals(3, counterA[0]);
		assertEquals(2, counterB[0]);
		
		//remove listener
		chA.removeStateChangedListener(listenerA);
		chA.moveState();
		assertEquals(3, counterA[0]);		
	}
	
}

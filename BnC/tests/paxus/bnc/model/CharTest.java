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
		Char ch = Char.valueOf('a', la);
		final int[] counter = {0};
		final IStateChangedListener listener = new IStateChangedListener() {
			public void onStateChanged(Character ch, ENCharState newState) {
				counter[0]++;
			}
		};
		
		//no listeners 
		ch.moveState();
		assertEquals(0, counter[0]);

		ch.addStateChangedListener(listener);
		
		//change by Char
		ch.moveState();
		assertEquals(1, counter[0]);

		//change by alphabet
		la.moveCharState(ch.ch);
		assertEquals(2, counter[0]);
		
		//no allowed states - should not change state and notify listeners
		ch.moveState(ENCharState.NONE, ENCharState.ABSENT, ENCharState.PRESENT);
		assertEquals(2, counter[0]);

		ch.removeStateChangedListener(listener);
		ch.moveState();
		assertEquals(2, counter[0]);
	}
	
}

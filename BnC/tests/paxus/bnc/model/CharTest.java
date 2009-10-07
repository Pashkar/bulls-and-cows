package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;

public class CharTest extends TestCase {

	public void testNull() throws BncException {
		Char ch = Char.NO_ALPHA;
		
		assertEquals('?', ch.ch);
		assertEquals("?", ch.asString);
		
		assertEquals(ENCharState.ABSENT, ch.moveState(ICharStateSequencer.FORWARD));
		assertEquals(ENCharState.PRESENT, ch.moveState(ICharStateSequencer.FORWARD));
		assertEquals(ENCharState.NONE, ch.moveState(ICharStateSequencer.FORWARD));

		assertEquals(ENCharState.NONE, ch.getState());
		
		//try null alphabet
		boolean ok = false;
		try {new Char('a', null); } 
		catch (NullPointerException e) {ok = true;}
		if (!ok)
			throw new BncException();
	}
}

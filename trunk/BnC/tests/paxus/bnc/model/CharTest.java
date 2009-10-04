package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;

public class CharTest extends TestCase {

	public void testNull() throws BncException {
		Char ch = Char.NULL;
		
		assertEquals('?', ch.ch);
		assertEquals("?", ch.asString);
		
		boolean ok = false;
		try {ch.moveState(ICharStateSequencer.FORWARD); } 
		catch (UnsupportedOperationException e) {ok = true;}
		if (!ok)
			throw new BncException();

		assertEquals(ENCharState.NONE, ch.getState());
		
		//try null alphabet
		ok = false;
		try {new Char('a', null); } 
		catch (NullPointerException e) {ok = true;}
		if (!ok)
			throw new BncException();
	}
}

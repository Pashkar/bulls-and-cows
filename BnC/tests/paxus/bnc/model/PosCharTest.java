package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;

public class PosCharTest extends TestCase {

	public void testPosChar() throws BncException {
		Alphabet.Latin la = new Alphabet.Latin();
		PositionTable table = new PositionTable(1, 5);
		PosChar pchA = new PosChar(Char.valueOf('a', la), 0, table);
		PosChar pchB = new PosChar(Char.valueOf('a', la), 0, table);
		assertNotNull(pchA);
		assertNotNull(pchB);
		assertTrue(pchA != pchB);	//check not one instance, possible to put several PosChar in s PosLine
	}
	
	public void testNull() throws BncException {
		assertNotNull(PosChar.NULL);
		assertTrue(PosChar.NULL.ch == Char.NO_ALPHA);
		
		Alphabet.Latin la = new Alphabet.Latin();
		
		boolean ok = false;
		try { new PosChar(Char.valueOf(new Character('a'), la), 0, null);}
		catch (NullPointerException npe) { ok = true;}
		assertTrue(ok);
	}
}

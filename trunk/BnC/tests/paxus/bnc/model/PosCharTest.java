package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.RunExecutor;

public class PosCharTest extends TestCase {

	public void testPosChar() throws BncException {
		Alphabet.Latin la = new Alphabet.Latin();
		PositionTable table = new PositionTable(1, 5);
		PosChar pchA = new PosChar(new Character('a'), 0, table);
		PosChar pchB = new PosChar(new Character('a'), 0, table);
		assertNotNull(pchA);
		assertNotNull(pchB);
		assertTrue(pchA != pchB);	//check not one instance, possible to put several PosChar in s PosLine
	}
	
	public void testNull() throws BncException {
		assertNotNull(PosChar.NULL);
		assertTrue(PosChar.NULL.ch == Char.NULL_CHAR);
		
		Alphabet.Latin la = new Alphabet.Latin();
		
		boolean ok = false;
		try { new PosChar(new Character('a'), 0, null);}
		catch (NullPointerException npe) { ok = true;}
		assertTrue(ok);
	}
	
	public void testMovePosState() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(la, "abcd");
		PositionTable table = run.posTable;

		table.addLine(new Character('a'));
		table.addLine(new Character('b'));
		table.addLine(new Character('c'));
		table.addLine(new Character('d'));
		
		PosChar pchA0 = table.char2line.get(new Character('a')).chars[0];
		
		//change state to be sure that after following removing states are cleared
		assertEquals(ENCharState.ABSENT, pchA0.movePosState());
		assertEquals(3, table.removeLine(new Character('a')));
		assertEquals(4, table.addLine(new Character('a')));

		pchA0 = table.char2line.get(new Character('a')).chars[0];
		PosChar pchA1 = table.char2line.get(new Character('a')).chars[1];
		PosChar pchA2 = table.char2line.get(new Character('a')).chars[2];
		PosChar pchA3 = table.char2line.get(new Character('a')).chars[3];
		PosChar pchB0 = table.char2line.get(new Character('b')).chars[0];
		PosChar pchB3 = table.char2line.get(new Character('b')).chars[3];
		
		
		//all ABSENT in line not allowed
		assertEquals(ENCharState.ABSENT, pchA0.movePosState());
		assertEquals(ENCharState.ABSENT, pchA1.movePosState());
		assertEquals(ENCharState.ABSENT, pchA2.movePosState());
		assertEquals(ENCharState.PRESENT, pchA3.movePosState());
		assertEquals(ENCharState.NONE, pchA3.movePosState());
		assertEquals(ENCharState.PRESENT, pchA3.movePosState());
		
		//Only one PRESENT in column
		assertEquals(ENCharState.ABSENT, pchB3.movePosState());
		assertEquals(ENCharState.NONE, pchB3.movePosState());
		
		//If line with PRESENT removed, PRESENT in column allowed   
		table.removeLine(new Character('a'));
		assertEquals(ENCharState.ABSENT, pchB3.movePosState());
		assertEquals(ENCharState.PRESENT, pchB3.movePosState());
		
		//Only one PRESENT in line
		assertEquals(ENCharState.ABSENT, pchB0.movePosState());
		assertEquals(ENCharState.NONE, pchB0.movePosState());
		
		//If PRESENT in line removed, another PRESENT allowed
		assertEquals(ENCharState.NONE, pchB3.movePosState());
		assertEquals(ENCharState.ABSENT, pchB0.movePosState());
		assertEquals(ENCharState.PRESENT, pchB0.movePosState());
	}
}
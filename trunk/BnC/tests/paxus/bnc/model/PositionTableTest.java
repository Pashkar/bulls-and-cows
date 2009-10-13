package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.IPosStateChangedListener;
import paxus.bnc.controller.RunExecutor;

public class PositionTableTest extends TestCase {

	public void testPositionTable() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(la, "abcd");
		PositionTable table = run.posTable;

		table.addLine(Char.valueOf('a', run.alphabet));
		
		//duplicate char
		boolean ok = false;
		try{ table.addLine(Char.valueOf('a', run.alphabet));}
		catch (BncException e) {ok = true;}
		assertTrue(ok);
		
		table.addLine(Char.valueOf('b', run.alphabet));
		table.addLine(Char.valueOf('c', run.alphabet));
		table.addLine(Char.valueOf('d', run.alphabet));

		//too much lines
		ok = false;
		try{ table.addLine(Char.valueOf('e', run.alphabet));}
		catch (BncException e) {ok = true;}
		assertTrue(ok);
		
		//remove wrong char
		ok = false;
		try{ table.removeLine(Char.valueOf('z', run.alphabet));}
		catch (BncException e) {ok = true;}
		assertTrue(ok);
		
		//too big pos value
		ok = false;
		try{ table.movePosStateForChar(Char.valueOf('z', run.alphabet), 10);}
		catch (BncException e) {ok = true;}
		assertTrue(ok);
		
		//change state to be sure that after following removing states are cleared
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(Char.valueOf('a', run.alphabet), 0));
		
		assertEquals(3, table.removeLine(Char.valueOf('a', run.alphabet)));
		assertEquals(4, table.addLine(Char.valueOf('a', run.alphabet)));
		
		//all ABSENT in line not allowed
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(Char.valueOf('a', run.alphabet), 0));
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(Char.valueOf('a', run.alphabet), 1));
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(Char.valueOf('a', run.alphabet), 2));
		assertEquals(ENCharState.PRESENT, table.movePosStateForChar(Char.valueOf('a', run.alphabet), 3));
		assertEquals(ENCharState.NONE, table.movePosStateForChar(Char.valueOf('a', run.alphabet), 3));
		assertEquals(ENCharState.PRESENT, table.movePosStateForChar(Char.valueOf('a', run.alphabet), 3));
		
		//Only one PRESENT in column
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 3));
		assertEquals(ENCharState.NONE, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 3));
		
		//If line with PRESENT removed, PRESENT in column allowed   
		table.removeLine(Char.valueOf('a', run.alphabet));
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 3));
		assertEquals(ENCharState.PRESENT, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 3));
		
		//Only one PRESENT in line
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 0));
		assertEquals(ENCharState.NONE, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 0));
		
		//If PRESENT in line removed, another PRESENT allowed
		assertEquals(ENCharState.NONE, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 3));
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 0));
		assertEquals(ENCharState.PRESENT, table.movePosStateForChar(Char.valueOf('b', run.alphabet), 0));
	}
	
	public void testPosStateChangedListener() throws BncException {
		Alphabet.Latin la = new Alphabet.Latin();
		PositionTable table = new PositionTable(1, 3);
		table.setCss(ICharStateSequencer.FORWARD);
		Char charA = Char.valueOf('a', la);
		table.addLine(charA);

		final int[] counter = {0, 0};
		final IPosStateChangedListener listener = new IPosStateChangedListener() {
			public void onPosStateChanged(PosChar pch, ENCharState newState) {
				counter[pch.pos]++;
			}
		};
		
		//no listeners
		table.movePosStateForChar(charA, 0);
		assertEquals(0, counter[0]);
		
		table.addPosStateChangedListener(listener);
		
		//move by poschar
		table.char2line.get(charA).chars[0].movePosState();
		assertEquals(1, counter[0]);
		
		//move by table
		table.movePosStateForChar(charA, 0);
		assertEquals(2, counter[0]);
		
		//another poschar
		table.movePosStateForChar(charA, 1);
		assertEquals(1, counter[1]);
	}

}

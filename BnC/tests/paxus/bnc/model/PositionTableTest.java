package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
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
		if (!ok)
			throw new BncException();

		table.addLine(Char.valueOf('b', run.alphabet));
		table.addLine(Char.valueOf('c', run.alphabet));
		table.addLine(Char.valueOf('d', run.alphabet));

		//too much lines
		ok = false;
		try{ table.addLine(Char.valueOf('e', run.alphabet));}
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
	
		//remove wrong char
		ok = false;
		try{ table.removeLine(Char.valueOf('z', run.alphabet));}
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
		
		//change state to be sure that after following removing states are cleared
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(Char.valueOf('a', run.alphabet), 0));
		
		assertEquals(3, table.removeLine(Char.valueOf('a', run.alphabet)));
		assertEquals(4, table.addLine(Char.valueOf('a', run.alphabet)));
		
		//all ABSENT in line not allowed
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(Char.valueOf('a', run.alphabet), 0));
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(Char.valueOf('a', run.alphabet), 1));
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(Char.valueOf('a', run.alphabet), 2));
		assertEquals(ENCharState.PRESENT, table.moveStateForChar(Char.valueOf('a', run.alphabet), 3));
		assertEquals(ENCharState.NONE, table.moveStateForChar(Char.valueOf('a', run.alphabet), 3));
		assertEquals(ENCharState.PRESENT, table.moveStateForChar(Char.valueOf('a', run.alphabet), 3));
		
		//Only one PRESENT in column
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(Char.valueOf('b', run.alphabet), 3));
		assertEquals(ENCharState.NONE, table.moveStateForChar(Char.valueOf('b', run.alphabet), 3));
		
		//If line with PRESENT removed, PRESENT in column allowed   
		table.removeLine(Char.valueOf('a', run.alphabet));
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(Char.valueOf('b', run.alphabet), 3));
		assertEquals(ENCharState.PRESENT, table.moveStateForChar(Char.valueOf('b', run.alphabet), 3));
		
		//Only one PRESENT in line
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(Char.valueOf('b', run.alphabet), 0));
		assertEquals(ENCharState.NONE, table.moveStateForChar(Char.valueOf('b', run.alphabet), 0));
		
		//If PRESENT in line removed, another PRESENT allowed
		assertEquals(ENCharState.NONE, table.moveStateForChar(Char.valueOf('b', run.alphabet), 3));
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(Char.valueOf('b', run.alphabet), 0));
		assertEquals(ENCharState.PRESENT, table.moveStateForChar(Char.valueOf('b', run.alphabet), 0));
		
	}
}

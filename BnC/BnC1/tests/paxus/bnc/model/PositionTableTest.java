package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.RunExecutor;

public class PositionTableTest extends TestCase {

	public void testPositionTable() throws BncException {
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(Alphabet.LATIN, "abcd");
		PositionTable table = run.posTable;

		table.addLine(new Char('a', run.alphabet));
		
		//duplicate char
		boolean ok = false;
		try{ table.addLine(new Char('a', run.alphabet));}
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();

		table.addLine(new Char('b', run.alphabet));
		table.addLine(new Char('c', run.alphabet));
		table.addLine(new Char('d', run.alphabet));

		//too much lines
		ok = false;
		try{ table.addLine(new Char('e', run.alphabet));}
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
	
		//remove wrong char
		ok = false;
		try{ table.removeLine(new Char('z', run.alphabet));}
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
		
		//change state to be sure that after following removing states are cleared
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(new Char('a', run.alphabet), ICharStateSequencer.FORWARD, 0));
		
		assertEquals(3, table.removeLine(new Char('a', run.alphabet)));
		assertEquals(4, table.addLine(new Char('a', run.alphabet)));
		
		//all ABSENT in line not allowed
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(new Char('a', run.alphabet), ICharStateSequencer.FORWARD, 0));
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(new Char('a', run.alphabet), ICharStateSequencer.FORWARD, 1));
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(new Char('a', run.alphabet), ICharStateSequencer.FORWARD, 2));
		assertEquals(ENCharState.PRESENT, table.moveStateForChar(new Char('a', run.alphabet), ICharStateSequencer.FORWARD, 3));
		assertEquals(ENCharState.NONE, table.moveStateForChar(new Char('a', run.alphabet), ICharStateSequencer.FORWARD, 3));
		assertEquals(ENCharState.PRESENT, table.moveStateForChar(new Char('a', run.alphabet), ICharStateSequencer.FORWARD, 3));
		
		//Only one PRESENT in column
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 3));
		assertEquals(ENCharState.NONE, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 3));
		
		//If line with PRESENT removed, PRESENT in column allowed   
		table.removeLine(new Char('a', run.alphabet));
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 3));
		assertEquals(ENCharState.PRESENT, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 3));
		
		//Only one PRESENT in line
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 0));
		assertEquals(ENCharState.NONE, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 0));
		
		//If PRESENT in line removed, another PRESENT allowed
		assertEquals(ENCharState.NONE, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 3));
		assertEquals(ENCharState.ABSENT, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 0));
		assertEquals(ENCharState.PRESENT, table.moveStateForChar(new Char('b', run.alphabet), ICharStateSequencer.FORWARD, 0));
		
	}
}

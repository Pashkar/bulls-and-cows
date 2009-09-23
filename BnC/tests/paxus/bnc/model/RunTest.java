package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.RunExecutor;

public class RunTest extends TestCase {

	public void testOfferWord() throws BncException {
		RunExecutor re = new RunExecutor();
		re.startNewRun(Alphabet.DIGITAL, "1234");
		re.offerWord("4321");
		WordComparisonResult res = re.offerWord("1234");
		assertEquals(2, re.getRun().wordsCompared.size());
		assertTrue(res.guessed());
	}
	
	public void testMoveCharState() throws BncException {
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(Alphabet.DIGITAL, "01");

		//check that all chars were added to PosTable
		run.moveCharStateForward(new Char('1', run.alphabet));
		run.moveCharStateForward(new Char('1', run.alphabet));
		assertEquals(1, run.posTable.getLinesCount());
		
		run.moveCharStateForward(new Char('2', run.alphabet));
		run.moveCharStateForward(new Char('2', run.alphabet));
		assertEquals(2, run.posTable.getLinesCount());
		
		run.moveCharStateForward(new Char('1', run.alphabet));
		assertEquals(1, run.posTable.getLinesCount());
		run.moveCharStateForward(new Char('2', run.alphabet));
		assertEquals(0, run.posTable.getLinesCount());
	}
}

package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.RunExecutor;

public class CharStateSequencerTest extends TestCase {

	public void testForwardSequencer() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(la, "abcd");
		Char ch = Char.valueOf('a', run.alphabet);
		
		//simple cycle
		assertEquals(ENCharState.ABSENT, ch.moveState());
		assertEquals(ENCharState.PRESENT, ch.moveState());
		assertEquals(ENCharState.NONE, ch.moveState());
		
		//test forbidden states
		assertEquals(ENCharState.PRESENT, ch.moveState(ENCharState.ABSENT));
		assertEquals(ENCharState.ABSENT, ch.moveState(ENCharState.NONE));
		assertEquals(ENCharState.ABSENT, ch.moveState(ENCharState.PRESENT, ENCharState.NONE));
		assertEquals(ENCharState.ABSENT, ch.moveState(ENCharState.PRESENT, ENCharState.NONE, ENCharState.ABSENT));
	}
}

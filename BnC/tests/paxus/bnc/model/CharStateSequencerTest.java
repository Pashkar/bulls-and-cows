package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.Run;

public class CharStateSequencerTest extends TestCase {

	public void testForwardSequencer() throws BncException {
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(Alphabet.LATIN, "abcd");
		Char ch = new Char('a', run.alphabet);
		
		//simple cycle
		assertEquals(ENCharState.ABSENT, ch.moveState(ICharStateSequencer.FORWARD));
		assertEquals(ENCharState.PRESENT, ch.moveState(ICharStateSequencer.FORWARD));
		assertEquals(ENCharState.NONE, ch.moveState(ICharStateSequencer.FORWARD));
		
		//test forbidden states
		assertEquals(ENCharState.PRESENT, ch.moveState(ICharStateSequencer.FORWARD, ENCharState.ABSENT));
		assertEquals(ENCharState.ABSENT, ch.moveState(ICharStateSequencer.FORWARD, ENCharState.NONE));
		assertEquals(ENCharState.ABSENT, ch.moveState(ICharStateSequencer.FORWARD, ENCharState.PRESENT, ENCharState.NONE));
		assertEquals(ENCharState.ABSENT, ch.moveState(ICharStateSequencer.FORWARD, ENCharState.PRESENT, ENCharState.NONE, ENCharState.ABSENT));
	}
	
	public void testAlphabetSequencer() throws BncException {
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(Alphabet.LATIN, "abcd");

		//test too many PRESENT
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('x', ENCharState.ABSENT));
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('y', ENCharState.ABSENT));
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('z', ENCharState.ABSENT));
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('a', ENCharState.ABSENT));
		assertEquals(ENCharState.NONE, run.alphabet.moveCharState('b', ENCharState.ABSENT));	//failed to set another PRESENT
		assertEquals(ENCharState.NONE, run.alphabet.moveCharState('x'));	//clear one PRESENT
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('b', ENCharState.ABSENT));	//success
	}
}

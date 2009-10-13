package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.RunExecutor;

public class AlphabetTest extends TestCase {
	public void testAlphabetSequencer() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(la, "abcd");

		//test too many PRESENT
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('x', ENCharState.ABSENT));
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('y', ENCharState.ABSENT));
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('z', ENCharState.ABSENT));
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('a', ENCharState.ABSENT));
		assertEquals(ENCharState.NONE, run.alphabet.moveCharState('b', ENCharState.ABSENT));	//failed to set another PRESENT
		assertEquals(ENCharState.NONE, run.alphabet.moveCharState('x'));	//clear one PRESENT
		assertEquals(ENCharState.PRESENT, run.alphabet.moveCharState('b', ENCharState.ABSENT));	//success
	}
	
	public void testGetAllChars() {
		final Alphabet da = new Alphabet.Digital();
		assertEquals(10, da.getAllChars().size());
		final Alphabet la = new Alphabet.Latin();
		assertEquals(26, la.getAllChars().size());
	}
}

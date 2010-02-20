package paxus.bnc.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateChangedListener;
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
	
	public void testGetAllCharsSorted() {
		final Alphabet da = new Alphabet.Digital();
		Collection<Char> chars = da.getAllCharsSorted();
		assertEquals(10, chars.size());
		char chi = '0';
		for (Char ch : chars) 
			assertSame(da.getCharInstance(chi++), ch);
		
		final Alphabet la = new Alphabet.Latin();
		chars = la.getAllCharsSorted();
		assertEquals(26, chars.size());
		chi = 'a';
		for (Char ch : chars) 
			assertSame(la.getCharInstance(chi++), ch);
	}
	
	public void testAllCharStateChangeListener() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		Char chA = Char.valueOf('a', la);
		Char chB = Char.valueOf('b', la);
		final int[] counterA = {0};
		final ICharStateChangedListener listenerA = new ICharStateChangedListener() {
			public void onCharStateChanged(Character ch, ENCharState newState) {
				if (ch.equals(new Character('a')))
					counterA[0]++;
			}
		};
		final int[] counterB = {0};
		final ICharStateChangedListener listenerB = new ICharStateChangedListener() {
			public void onCharStateChanged(Character ch, ENCharState newState) {
				if (ch.equals(new Character('b')))
					counterB[0]++;
			}
		};
		
		//no listeners 
		chA.moveState();
		assertEquals(0, counterA[0]);

		la.addAllCharsStateChangedListener(listenerA);
		la.addAllCharsStateChangedListener(listenerB);
		
		//change by Char
		chA.moveState();
		assertEquals(1, counterA[0]);

		//change by alphabet
		la.moveCharState(chA.ch);
		assertEquals(2, counterA[0]);
		
		//no allowed states - should not change state and notify listeners
		chA.moveState(ENCharState.NONE, ENCharState.ABSENT, ENCharState.PRESENT);
		assertEquals(2, counterA[0]);

		//another char, another listener
		chB.moveState();
		assertEquals(2, counterA[0]);
		assertEquals(1, counterB[0]);
		
		//remove listener
		la.removeAllCharsStateCRhangedListener(listenerA);
		chA.moveState();
		assertEquals(2, counterA[0]);		
	}
	
	public void testSerialize() throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(baos);

		final Alphabet la = new Alphabet.Latin();
		assertEquals(ENCharState.ABSENT, la.getCharInstance('x').moveState());
		os.writeObject(la);
		os.close();
		
		ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		Alphabet a = (Alphabet) is.readObject();
		is.close();
		
		assertEquals("Latin", a.getName());
		assertEquals(ENCharState.NONE, a.getCharInstance('a').getState());
		assertEquals(ENCharState.ABSENT, a.getCharInstance('x').getState());
	}
}

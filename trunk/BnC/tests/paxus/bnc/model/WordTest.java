package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;

public class WordTest extends TestCase {

	public void testWord() throws BncException {
		final Alphabet da = new Alphabet.Digital();
		new Word(da, "123");

		final Alphabet la = new Alphabet.Latin();
		new Word(la, "aBc");
		
		//test duplicate
		boolean ok = false;
		try {new Word(la, "aBcA"); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();

		//test invalid char
		ok = false;
		try {new Word(la, "abc1"); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
	}
}

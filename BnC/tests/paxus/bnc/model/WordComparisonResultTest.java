package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.WordComparator;

public class WordComparisonResultTest extends TestCase {
	
	public void testWordComparisonResult() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		WordComparator wc = new WordComparator();
		
		//wrong length
		boolean ok = false;
		try {wc.compare(new Word(la, "abcd"), new Word(la, "abcde")); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
		
		//wrong alphabet
		ok = false;
		try {wc.compare(new Word(la, "abcd"), new Word(la, "1234")); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
		
		assertTrue(wc.compare(new Word(la, "abcd"), new Word(la, "abcd")).guessed());
		WordComparisonResult res = wc.compare(new Word(la, "abcd"), new Word(la, "zbdc"));
		assertEquals(1, res.bullsCount);
		assertEquals(2, res.cowsCount);
		assertFalse(res.guessed());
	}
}

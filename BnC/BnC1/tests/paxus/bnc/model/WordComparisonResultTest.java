package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.WordComparator;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Word;
import paxus.bnc.model.WordComparisonResult;

public class WordComparisonResultTest extends TestCase {
	
	public void testWordComparisonResult() throws BncException {
		WordComparator wc = new WordComparator();
		
		//wrong length
		boolean ok = false;
		try {wc.compare(new Word(Alphabet.LATIN, "abcd"), new Word(Alphabet.LATIN, "abcde")); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
		
		//wrong alphabet
		ok = false;
		try {wc.compare(new Word(Alphabet.LATIN, "abcd"), new Word(Alphabet.DIGITAL, "1234")); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
		
		assertTrue(wc.compare(new Word(Alphabet.LATIN, "abcd"), new Word(Alphabet.LATIN, "abcd")).guessed());
		WordComparisonResult res = wc.compare(new Word(Alphabet.LATIN, "abcd"), new Word(Alphabet.LATIN, "zbdc"));
		assertEquals(1, res.bullsCount);
		assertEquals(2, res.cowsCount);
		assertFalse(res.guessed());
	}
}

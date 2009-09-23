package paxus.bnc.model;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Word;

public class WordTest extends TestCase {

	public void testWord() throws BncException {
		new Word(Alphabet.LATIN, "aBc");
		new Word(Alphabet.DIGITAL, "123");
		new Word(Alphabet.CYRILLIC, "‡¡‚");
		
		//test duplicate
		boolean ok = false;
		try {new Word(Alphabet.LATIN, "aBcA"); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();

		//test invalid char
		ok = false;
		try {new Word(Alphabet.LATIN, "abc1"); } 
		catch (BncException e) {ok = true;}
		if (!ok)
			throw new BncException();
	}
}

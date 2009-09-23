package paxus.bnc.controller;

import paxus.bnc.BncException;
import paxus.bnc.model.Word;
import paxus.bnc.model.WordComparisonResult;

public final class WordComparator {

	public WordComparisonResult compare(Word w1, Word w2) throws BncException {
		if (w1.wordLength != w2.wordLength)
			throw new BncException("Words must have coincident length");
		if (w1.alphabet != w2.alphabet)
			throw new BncException("Words must have coincident alphabet");
			
		String w1str = w1.asString();
		String w2str = w2.asString();
		int bulls = 0;
		int cows = 0;
		for (int i = 0; i < w1str.length(); i++) {
			int pos = w2str.indexOf(w1str.charAt(i));
			if (pos < 0) 
				continue;

			if (pos == i)
				bulls++;
			else
				cows++;
		}
		
		return new WordComparisonResult(w2str.length(), cows, bulls);
	}
}

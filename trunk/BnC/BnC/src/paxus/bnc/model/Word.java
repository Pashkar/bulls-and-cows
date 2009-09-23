package paxus.bnc.model;

import java.util.HashSet;
import java.util.Set;

import paxus.bnc.BncException;

public final class Word {

	public final Char[] word = new Char[Run.MAX_WORD_LENGTH];
	
	public final int wordLength;

	public final Alphabet alphabet;

	public Word(Alphabet alphabet, String word) throws BncException {
		this.alphabet = alphabet;
		if (word.length() > Run.MAX_WORD_LENGTH)
			throw new BncException("Word is loo long (" + word.length() + ")");
		this.wordLength = word.length(); 
		
		Set<Character> chars = new HashSet<Character>();
		int i = 0;
		for (char ch : word.toLowerCase().toCharArray()) {
			//check that no duplicates
			if (chars.contains(ch))	
				throw new BncException("Duplicate chars \"" + ch + "\" in word \"" + word + "\"");
			chars.add(ch);
			
			this.word[i++] = new Char(ch, alphabet);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < wordLength; i++) {
			sb.append(word[i] + "");
		}
		return sb.toString();
	}
	
	public String asString() {
		StringBuilder sb = new StringBuilder(wordLength);
		for (int i = 0; i < wordLength; i++) {
			sb.append(word[i].ch);
		}
		return sb.toString();
	}
}

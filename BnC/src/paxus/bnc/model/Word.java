package paxus.bnc.model;

import java.util.HashSet;

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
		
		Char[] mWord = this.word;
		HashSet<Character> chars = new HashSet<Character>();
		char[] charArray = word.toLowerCase().toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char ch = charArray[i];
			//check that no duplicates
			if (chars.contains(ch))	
				throw new BncException("Duplicate chars \"" + ch + "\" in word \"" + word + "\"");
			chars.add(ch);
			mWord[i] = new Char(ch, alphabet);
		}
	}

	@Override
	public String toString() {
		Char[] mWord = word;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < wordLength; i++) {
			sb.append(mWord[i] + "");
		}
		return sb.toString();
	}
	
	public String asString() {
		Char[] mWord = word;
		StringBuilder sb = new StringBuilder(wordLength);
		for (int i = 0; i < wordLength; i++) {
			sb.append(mWord[i].ch);
		}
		return sb.toString();
	}
}

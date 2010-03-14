package paxus.bnc.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import paxus.bnc.BncException;

public final class Word 
/*implements Serializable - can't be serialized individually, only with Alphabet and bulk of Chars. 
 * Serialization to be managed externally using helpers methods write() and read(). */ {

	public final Char[] chars = new Char[Run.MAX_WORD_LENGTH];
	
	public final int wordLength;

	public transient final Alphabet alphabet;

	public Word(Alphabet alphabet, String word) throws BncException {
		this.alphabet = alphabet;
		if (word.length() > Run.MAX_WORD_LENGTH)
			throw new BncException("Word is too long (" + word.length() + ")");
		this.wordLength = word.length(); 
		
		Char[] mWord = this.chars;
		StringBuilder chars = new StringBuilder(wordLength);
		char[] charArray = word.toLowerCase().toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char ch = charArray[i];
			//check that no duplicates
			if (chars.indexOf(ch + "") >= 0)	
				throw new BncException("Duplicate chars \"" + ch + "\" in word \"" + word + "\"");
			chars.append(ch);
			try {
				mWord[i] = Char.valueOf(ch, alphabet);
			} catch (BncException e) {
				throw new BncException("Word \"" + word + "\": wrong symbol at position " + i, e);
			}
		}
	}

	@Override
	public String toString() {
		Char[] mWord = chars;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < wordLength; i++) {
			sb.append(mWord[i] + "");
		}
		return sb.toString();
	}
	
	public String asString() {
		Char[] mWord = chars;
		StringBuilder sb = new StringBuilder(wordLength);
		for (int i = 0; i < wordLength; i++) {
			sb.append(mWord[i].ch);
		}
		return sb.toString();
	}

	public static Word read(ObjectInput in, int wordLength, Alphabet alphabet) throws IOException, BncException {
		StringBuilder sb = new StringBuilder(wordLength);
		for (int i = 0; i < wordLength; i++)
			sb.append(in.readChar());
		Word word = new Word(alphabet, sb.toString());
		return word;
	}

	public void write(ObjectOutput out) throws IOException {
		final Char[] chars = this.chars;
		for (int i = 0; i < wordLength; i++) {
			out.writeChar(chars[i].ch);
		}
	}
}

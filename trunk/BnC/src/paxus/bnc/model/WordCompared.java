package paxus.bnc.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import paxus.bnc.BncException;

public class WordCompared {
	public Word word;
	public WordComparisonResult result;

	public WordCompared(Word word, WordComparisonResult result) {
		this.word = word;
		this.result = result;
	}

	@Override
	public String toString() {
		return word + " -> " + result;
	}

	public static WordCompared read(ObjectInput in, int wordLength, Alphabet alphabet) 
			throws IOException, ClassNotFoundException {
		Word word = null;
		try {word = Word.read(in, wordLength, alphabet); } catch (BncException e) {}
		WordComparisonResult wcr = (WordComparisonResult) in.readObject();
		return new WordCompared(word, wcr);
	}

	public void write(ObjectOutput out) throws IOException {
		word.write(out);
		out.writeObject(result);
	}
}

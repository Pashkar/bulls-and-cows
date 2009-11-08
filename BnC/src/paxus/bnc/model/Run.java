package paxus.bnc.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;
import java.util.List;

import paxus.bnc.BncException;

public final class Run implements Externalizable {

	public static final int MAX_WORD_LENGTH = 10;
	
	public int wordLength;
	
	public Alphabet alphabet;
	
	public Word secret;
	
	public PositionTable posTable;
	
	public final List<WordCompared> wordsCompared = new LinkedList<WordCompared>();
	
	public Run() {
	}
	
	public Run(Alphabet alphabet, String secret) throws BncException {
		this.alphabet = alphabet;
		this.secret = new Word(alphabet, secret);
		this.wordLength = secret.length();
		this.posTable = new PositionTable(wordLength, wordLength);
	}
	
	public void addWordCompared(WordCompared wordCompared) {
		wordsCompared.add(wordCompared);
	}
	
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		wordLength = in.readInt();
		alphabet = (Alphabet) in.readObject();
		posTable = (PositionTable) in.readObject();
		try { secret = Word.read(in, wordLength, alphabet); } catch (BncException e) {
		} 
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(wordLength);
		out.writeObject(alphabet);
		out.writeObject(posTable);
		secret.write(out);
		//TODO wordsCompared
	}
	
	public final class WordCompared {
		public final Word word;
		public final WordComparisonResult result;

		public WordCompared(Word word, WordComparisonResult result) {
			super();
			this.word = word;
			this.result = result;
		}

		@Override
		public String toString() {
			return word + " -> " + result;
		}
	}
}

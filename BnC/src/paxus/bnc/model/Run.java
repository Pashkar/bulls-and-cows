package paxus.bnc.model;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import paxus.bnc.BncException;

public final class Run implements Externalizable {

	public static final int MIN_WORD_LENGTH = 3;
	public static final int MAX_WORD_LENGTH = 7;
	
	public int wordLength;
	public static Alphabet alphabet;
	public Word secret;
	
	public PositionTable posTable;
	public LinkedList<WordCompared> wordsCompared = new LinkedList<WordCompared>();
	
	public ExtraData data = new ExtraData();
	
	/**
	 *	For serialization purposes only 
	 */
	public Run() {
	}
	
	public Run(Alphabet alphabet, String secret) throws BncException {
		Run.alphabet = alphabet;
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
		alphabet.addAllCharsStateChangedListener(posTable);
		try { secret = Word.read(in, wordLength, alphabet); } catch (BncException e) {}
		
		final LinkedList<WordCompared> wordsCompared2 = wordsCompared;
		wordsCompared2.clear();
		int wcCount = in.readInt();
		for (int i = 0; i < wcCount; i++)
			wordsCompared.add(readWordCompared(in, wordLength, alphabet));
		
		data = (ExtraData) in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(wordLength);
		out.writeObject(alphabet);
		out.writeObject(posTable);
		secret.write(out);
		
		LinkedList<WordCompared> wordsCompared2 = wordsCompared;
		out.writeInt(wordsCompared2.size());
		for (WordCompared wc : wordsCompared2) 
			writeWordCompared(out, wc);

		out.writeObject(data);
	}
	
	public WordCompared readWordCompared(ObjectInput in, int wordLength,
			Alphabet alphabet) throws IOException, ClassNotFoundException {
		Word word = null;
		try {
			word = Word.read(in, wordLength, alphabet);
		} catch (BncException e) {
		}
		WordComparisonResult result = (WordComparisonResult) in.readObject();
		return this.new WordCompared(word, result);
	}

	public void writeWordCompared(ObjectOutput out, WordCompared wc) throws IOException {
		wc.word.write(out);
		out.writeObject(wc.result);
	}
	
	public void clearMarks() throws BncException {
		for (Char ch : alphabet.getAllChars())
			ch.moveState(ENCharState.ABSENT, ENCharState.PRESENT);
	}
	
	public final class WordCompared {
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
	}

	/**
	 * Additional data to be serialized within the Run instance.
	 */
	@SuppressWarnings("serial")
	public static final class ExtraData implements Serializable {
		public static final String DATA_GIVEN_UP = "givenUp";
		public static final String DATA_SECRET_LINE = "secretLine";
		public static final String DATA_INTRODUCTION_SHOWN = "showIntroduction";

		public final Map<String, Object> map = new HashMap<String, Object>();	//additional data to be serialized
	}
	
}

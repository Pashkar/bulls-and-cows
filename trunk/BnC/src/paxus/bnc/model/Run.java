package paxus.bnc.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;

import paxus.bnc.BncException;

public final class Run implements Externalizable {

	public static final int MIN_WORD_LENGTH = 3;
	public static final int MAX_WORD_LENGTH = 7;
	
	public int wordLength;
	
	public Alphabet alphabet;
	
	public Word secret;
	
	public Char[] secretLine = new Char[MAX_WORD_LENGTH];	//store displayed secret line to serialize it in complex
	
	public PositionTable posTable;
	
	public LinkedList<WordCompared> wordsCompared = new LinkedList<WordCompared>();
	
	public Run() {
	}
	
	public Run(Alphabet alphabet, String secret) throws BncException {
		this.alphabet = alphabet;
		this.secret = new Word(alphabet, secret);
		this.wordLength = secret.length();
		this.posTable = new PositionTable(wordLength, wordLength);
		for (int i = 0; i < wordLength; i++)
			this.secretLine[i] = Char.NULL;
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
		
		try {
			for (int i = 0; i < wordLength; i++)
				secretLine[i] = Char.valueOf(in.readChar(), alphabet);
		} catch (BncException e) {} 
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
		
		for (int i = 0; i < wordLength; i++)
			out.writeChar(secretLine[i].ch);
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

	public void clearMarks() throws BncException {
		for (Char ch : alphabet.getAllChars())
			ch.moveState(ENCharState.ABSENT, ENCharState.PRESENT);
	}
}

package paxus.bnc.model;

import java.util.LinkedList;
import java.util.List;

import paxus.bnc.BncException;

public final class Run {

	public static final int MAX_WORD_LENGTH = 20;
	
	public final int wordLength;
	
	public final Alphabet alphabet;
	
	public final Word secret;
	
	public final PositionTable posTable;
	
	public final List<WordCompared> wordsCompared = new LinkedList<WordCompared>();
	
	public Run(Alphabet alphabet, String secret) throws BncException {
		this.alphabet = alphabet;
		this.secret = new Word(alphabet, secret);
		this.wordLength = secret.length();
		this.posTable = new PositionTable(wordLength);
	}
	
	public void addWordCompared(WordCompared wordCompared) {
		wordsCompared.add(wordCompared);
	}

	public ENCharState moveCharStateForward(Char ch) throws BncException {
		ENCharState oldState = ch.getState();
		ENCharState newState = ch.moveState();
		if (newState != oldState) {
			if (newState == ENCharState.PRESENT) {
				posTable.addLine(ch);
			} else if (oldState == ENCharState.PRESENT) {
				posTable.removeLine(ch);
			}
		}
		return newState;
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

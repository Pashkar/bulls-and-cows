package paxus.bnc.controller;

import paxus.bnc.BncException;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.Run;
import paxus.bnc.model.Word;
import paxus.bnc.model.WordComparisonResult;

public final class RunExecutor {
	
	private Run run;
	public Run getRun() {
		return run;
	}	
	
	private final static WordComparator wc = new WordComparator(); 
	
	public Run startNewRun(Alphabet alphabet, String secret) throws BncException {
		this.run = new Run(alphabet,  secret);
		run.alphabet.setDefaultCss(new LimitedStateSequencer(ICharStateSequencer.FORWARD, run.wordLength, ENCharState.PRESENT, run.alphabet));
		return this.run; 
	}
	
	public WordComparisonResult offerWord(String str) throws BncException {
		Word word = new Word(run.alphabet, str);
		WordComparisonResult res = wc.compare(run.secret, word);
		run.addWordCompared(run.new WordCompared(word, res));
		
		//TODO fail game when limit exceeds? or endless?
		return res;
	}

	private void winGame() {
		//FIXME winGame
		System.out.println("WIN THE GAME");
	}
}

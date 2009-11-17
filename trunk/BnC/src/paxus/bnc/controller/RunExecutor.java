package paxus.bnc.controller;

import paxus.bnc.BncException;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.Run;
import paxus.bnc.model.Word;
import paxus.bnc.model.WordCompared;
import paxus.bnc.model.WordComparisonResult;

public final class RunExecutor {
	
	private Run run;
	public Run getRun() {
		return run;
	}	
	
	private final static WordComparator wc = new WordComparator(); 
	
	public Run startNewRun(Alphabet alphabet, String secret) throws BncException {
		alphabet.clear();
		this.run = new Run(alphabet,  secret);
		
		//allow PosTable listen to changes to automatically add/remove row on char marked/unmarked as PRESENT
		this.run.alphabet.addAllCharsStateChangedListener(this.run.posTable);
		
		//Not more then wordLength PRESENT allowed
		run.alphabet.setCss(
				new LimitedStateSequencer(
						ICharStateSequencer.FORWARD, 
						ENCharState.PRESENT, 
						run.wordLength, 
						run.alphabet));
		 
		//Only one PRESENT in line/column allowed. <br/>
		//All chars with ABSENT in line/column not allowed.
		run.posTable.setCss(
				new LimitedStateSequencer(	//limit of ABSENT
						new LimitedStateSequencer(ICharStateSequencer.FORWARD, ENCharState.PRESENT, 1, run.posTable),	//limit of PRESENT
						ENCharState.ABSENT, 
						run.wordLength - 1, 
						run.posTable)
				);
		return this.run; 
	}
	
	public WordComparisonResult offerWord(String str) throws BncException {
		Word word = new Word(run.alphabet, str);
		return offerWord(word);
	}
	
	public WordComparisonResult offerWord(Word word) throws BncException {
		WordComparisonResult res = wc.compare(run.secret, word);
		run.addWordCompared(new WordCompared(word, res));
		
		//TODO fail game when limit exceeds? or endless?
		return res;
	}

	private void winGame() {
		//FIXME winGame
		System.out.println("WIN THE GAME");
	}
}

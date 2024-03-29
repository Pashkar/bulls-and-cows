package paxus.bnc.controller;

import paxus.bnc.BncException;
import paxus.bnc.model.*;

public final class RunExecutor {
	
	public Run run;
	
	private final static WordComparator wc = new WordComparator(); 
	
	public Run startNewRun(Alphabet alphabet, String secret) throws BncException {
		alphabet.clear();
		this.run = new Run(alphabet,  secret);
		
		//allow PosTable listen to changes to automatically add/remove row on char marked/unmarked as PRESENT
		Run.alphabet.addAllCharsStateChangedListener(this.run.posTable);
		
		//Not more then wordLength PRESENT allowed
		Run.alphabet.setCss(
				new LimitedStateSequencer(
						ICharStateSequencer.FORWARD, 
						ENCharState.PRESENT, 
						run.wordLength, 
						Run.alphabet));
		 
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
	
	public Run.WordCompared offerWord(String str) throws BncException {
		Word word = new Word(Run.alphabet, str);
		WordComparisonResult res = wc.compare(run.secret, word);
		final Run.WordCompared wordCompared = run.new WordCompared(word, res);
		run.addWordCompared(wordCompared);
		
		//TODO fail game when limit exceeds? or endless?
		return wordCompared;
	}
	
	public void giveUp() {
		run.data.map.put(Run.ExtraData.DATA_GIVEN_UP, true);
	}
}

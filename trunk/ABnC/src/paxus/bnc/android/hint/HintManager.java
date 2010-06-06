package paxus.bnc.android.hint;


public class HintManager {

	public Hint guess;

	public synchronized Hint createInstance(Hint hintInstance, IHintView view) {
		guess = Hint.GUESS.createInstance(view); 
		return guess;
	}
	
}

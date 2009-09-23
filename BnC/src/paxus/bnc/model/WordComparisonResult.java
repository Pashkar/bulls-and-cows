package paxus.bnc.model;

/**
 * immutable
 */
public final class WordComparisonResult {
	
	private final int wordLength;
	
	public final int cowsCount;

	public final int bullsCount;
	
	public WordComparisonResult(int wordLength, int cowsCount, int bullsCount) {
		this.wordLength = wordLength;
		this.cowsCount = cowsCount;
		this.bullsCount = bullsCount;
	}

	public boolean guessed() {
		return bullsCount == wordLength; 
	}

	@Override
	public String toString() {
		return String.format("bulls: %d, cows: %d, guessed: %b", bullsCount, cowsCount, guessed());
	}
}

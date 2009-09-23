package paxus.bnc.model;

public enum ENCharState {
	
	NONE("_"), 
	ABSENT("X"),	 
	PRESENT("*");
	
	private final String title;
	
	private ENCharState(String title) {
		this.title = title;
	}
	
	public String toString() {
		return "[" + title + "]";
	}
}

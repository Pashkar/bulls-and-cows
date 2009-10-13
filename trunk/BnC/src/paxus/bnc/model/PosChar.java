package paxus.bnc.model;

public final class PosChar {
	public final Char ch;
	
	ENCharState state = ENCharState.NONE;	//to be manipulated in special way as it's not an ordinal Char
	
	public final PositionTable table; //is used?
	
	public final int pos;

	//package-private
	//to create from PositionTable or test
	PosChar(Char ch, int pos, PositionTable table) {
		if (table == null && ch != Char.NO_ALPHA)
			throw new NullPointerException("PositionTable cannot be null");
		this.ch = ch;
		this.table = table;
		this.pos = pos;
	}
	
	public void movePosState() {
		table.movePosState(this);
	}

	@Override
	public String toString() {
		return state + ""; 
	}
	
	public static final PosChar NULL = new PosChar(Char.NO_ALPHA, -1, null);
}

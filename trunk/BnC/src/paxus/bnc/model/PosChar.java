package paxus.bnc.model;

import java.util.ArrayList;

import paxus.bnc.controller.IPosCharStateChangedListener;

public class PosChar {
	public final Character ch;
	
	ENCharState state = ENCharState.NONE;	//to be manipulated in special way as it's not an ordinal Char
	
	public final PositionTable table; //is used?
	
	public final int pos;
	
	private final ArrayList<IPosCharStateChangedListener> stateChangedListenerList = new ArrayList<IPosCharStateChangedListener>();

	//package-private
	//to create from PositionTable or test
	PosChar(Character ch, int pos, PositionTable table) {
		if (table == null && ch != Char.NULL_CHAR)
			throw new NullPointerException("PositionTable cannot be null");
		this.ch = ch;
		this.table = table;
		this.pos = pos;
	}
	
	public ENCharState movePosState() {
		return table.movePosState(this);
	}

	public void addPosStateChangedListener(IPosCharStateChangedListener listener) {
		this.stateChangedListenerList.add(listener);
	}
	
	public void removePosStateChangedListener(IPosCharStateChangedListener listener) {
		this.stateChangedListenerList.remove(listener);
	}
	
	public void onPosStateChanged(PosChar pch, ENCharState newState) {
		for (IPosCharStateChangedListener listener : stateChangedListenerList) 
			listener.onPosCharStateChanged(pch, newState);		
	}

	
	@Override
	public String toString() {
		return "" + ch + state; 
	}
	
	public static final PosChar NULL = new PosChar(Char.NULL_CHAR, -1, null) {
		@Override
		public ENCharState movePosState() {
			return ENCharState.NONE;
		}
	};

}

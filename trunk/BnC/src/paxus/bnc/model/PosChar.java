package paxus.bnc.model;

import java.util.ArrayList;

import paxus.bnc.controller.IPosStateChangedListener;

public final class PosChar {
	public final Char ch;
	
	ENCharState state = ENCharState.NONE;	//to be manipulated in special way as it's not an ordinal Char
	
	public final PositionTable table; //is used?
	
	public final int pos;
	
	private final ArrayList<IPosStateChangedListener> stateChangedListenerList = new ArrayList<IPosStateChangedListener>();

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

	public void addPosStateChangedListener(IPosStateChangedListener listener) {
		this.stateChangedListenerList.add(listener);
	}
	
	public void removePosStateChangedListener(IPosStateChangedListener listener) {
		this.stateChangedListenerList.remove(listener);
	}
	
	public void onPosStateChanged(PosChar pch, ENCharState newState) {
		for (IPosStateChangedListener listener : stateChangedListenerList) 
			listener.onPosStateChanged(pch, newState);		
	}

	
	@Override
	public String toString() {
		return state + ""; 
	}
	
	public static final PosChar NULL = new PosChar(Char.NO_ALPHA, -1, null);

}

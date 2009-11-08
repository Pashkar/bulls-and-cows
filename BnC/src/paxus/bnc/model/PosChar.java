package paxus.bnc.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import paxus.bnc.controller.IPosCharStateChangedListener;

public class PosChar implements Externalizable {
	public Character ch;
	
	public ENCharState state = ENCharState.NONE;	//to be manipulated in special way as it's not an ordinal Char
	
	public PositionTable table;
	
	public int pos;
	
	private transient final ArrayList<IPosCharStateChangedListener> stateChangedListenerList = new ArrayList<IPosCharStateChangedListener>();

	public PosChar() {
	}
	
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
		for (IPosCharStateChangedListener listener : stateChangedListenerList) { 
			listener.onPosCharStateChanged(pch, newState);		
		}
	}
	
	@Override
	public String toString() {
		return "" + ch + state + "[" + pos + "]"; 
	}
	
	public static final PosChar NULL = new PosChar(Char.NULL_CHAR, -1, null) {
		@Override
		public ENCharState movePosState() {
			return ENCharState.NONE;
		}
	};

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		ch = in.readChar();
		state = (ENCharState) in.readObject();
		pos = in.readInt();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeChar(ch);
		out.writeObject(state);
		out.writeInt(pos);
	}

}

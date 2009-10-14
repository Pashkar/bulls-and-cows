package paxus.bnc.controller;

import paxus.bnc.model.PositionTable.PositionLine;

public interface IPositionTableListener {

	public void onPosTableUpdate(boolean insert, Character ch, PositionLine line);
	
}

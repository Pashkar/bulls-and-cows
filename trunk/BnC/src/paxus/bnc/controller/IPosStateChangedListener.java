package paxus.bnc.controller;

import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;

public interface IPosStateChangedListener {

	public void onPosStateChanged(PosChar ch, ENCharState newState);

}
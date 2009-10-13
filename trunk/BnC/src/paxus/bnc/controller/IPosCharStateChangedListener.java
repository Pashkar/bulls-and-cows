package paxus.bnc.controller;

import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;

public interface IPosCharStateChangedListener {

	public void onPosCharStateChanged(PosChar ch, ENCharState newState);

}
package paxus.bnc.controller;

import paxus.bnc.model.ENCharState;

public interface IStateChangedListener {

	public void onStateChanged(Character ch, ENCharState newState);

}
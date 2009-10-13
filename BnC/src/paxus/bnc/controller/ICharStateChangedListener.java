package paxus.bnc.controller;

import paxus.bnc.model.ENCharState;

public interface ICharStateChangedListener {

	public void onCharStateChanged(Character ch, ENCharState newState);

}
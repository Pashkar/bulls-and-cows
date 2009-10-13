package paxus.bnc.controller;

import paxus.bnc.BncException;
import paxus.bnc.model.ENCharState;

public interface ICharStateChangedListener {

	public void onCharStateChanged(Character ch, ENCharState newState) throws BncException;

}
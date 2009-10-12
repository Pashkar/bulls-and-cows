package paxus.bnc.controller;

import paxus.bnc.model.ENCharState;

public interface IStateChangedListener {

	public abstract void onStateChanged(Character ch, ENCharState newState);

}
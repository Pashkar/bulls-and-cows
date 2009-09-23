package paxus.bnc.controller;


import paxus.bnc.model.ENCharState;

public interface ICharStateSequencer {
	
	public ENCharState nextState(ENCharState curState, ENCharState... forbidden);
	
	/**
	 * NONE -> ABSENT -> PRESENT -> NONE
	 */
	public static final ICharStateSequencer FORWARD = new ForwardSequencer();
}

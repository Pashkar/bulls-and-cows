package paxus.bnc.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import paxus.bnc.model.ENCharState;

/**
 * NONE -> ABSENT -> PRESENT -> NONE
 */
public class ForwardSequencer implements ICharStateSequencer {
	public ENCharState nextState(ENCharState curState, ENCharState... forbidden) {
		HashSet<ENCharState> forbSet = new HashSet<ENCharState>();
		for (int i = 0; i < forbidden.length; i++) {
			forbSet.add(forbidden[i]);
		}
		
		ENCharState state = curState;
		do {
			switch (state) {
			case NONE: 
				state = ENCharState.ABSENT;
				break;
			case ABSENT:
				state = ENCharState.PRESENT;
				break;
			case PRESENT:
				state = ENCharState.NONE;
				break;
			}
		} while (forbSet.contains(state) && state != curState);	
		return state;
	}
}
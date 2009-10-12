package paxus.bnc.controller;


import java.util.HashSet;

import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;

public interface ICharStateSequencer {
	
	public ENCharState nextState(ENCharState curState, Char ch, int pos, ENCharState... forbidden);
	
	/**
	 * NONE -> ABSENT -> PRESENT -> NONE
	 */
	public static final ICharStateSequencer FORWARD = new ICharStateSequencer() {
		public ENCharState nextState(ENCharState curState, Char ch, int pos, ENCharState... forbidden) {
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
	};
	
}

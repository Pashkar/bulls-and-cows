package paxus.bnc.controller;


import java.io.Serializable;
import java.util.HashSet;

import paxus.bnc.model.ENCharState;

@SuppressWarnings("serial")
public interface ICharStateSequencer extends Serializable {
	
	public ENCharState nextState(ENCharState curState, Character ch, int pos, ENCharState... forbidden);
	
	/**
	 * NONE -> ABSENT -> PRESENT -> NONE
	 */
	public static final ICharStateSequencer FORWARD = new ICharStateSequencer() {
		public ENCharState nextState(ENCharState curState, Character ch, int pos, ENCharState... forbidden) {
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

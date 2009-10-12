package paxus.bnc.controller;

import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;

/**
 *	Limits amount of stated chars
 */
public final class LimitedStateSequencer implements ICharStateSequencer {

	private final ICharStateSequencer baseCss;
	private final int max;
	private final ENCharState state;
	private final IStatesCounter sc;

	public LimitedStateSequencer(ICharStateSequencer baseCss, ENCharState state, int max, IStatesCounter sc) {
		this.baseCss = baseCss;
		this.max = max;
		this.state = state;
		this.sc = sc; 
	}

	public ENCharState nextState(ENCharState curState, Char ch, int pos, ENCharState... forbidden) {
		ENCharState[] newForb = forbidden;
		if (sc.getStatesCount(state, ch, pos) >= max) {
			newForb = new ENCharState[forbidden.length + 1];
			for (int i = 0; i < forbidden.length; i++) {
				newForb[i] = forbidden[i];
			}
			newForb[newForb.length - 1] = state;
		}
		return baseCss.nextState(curState, ch, pos, newForb);
	}
}

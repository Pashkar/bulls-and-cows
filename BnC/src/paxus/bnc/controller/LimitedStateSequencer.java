package paxus.bnc.controller;

import java.util.Arrays;

import paxus.bnc.model.ENCharState;
import paxus.bnc.model.IStatesCounter;

/**
 *	Limits amount of stated chars
 */
public final class LimitedStateSequencer implements ICharStateSequencer {

	private final ICharStateSequencer baseCss;
	private final int max;
	private final ENCharState state;
	private final IStatesCounter sc;

	public LimitedStateSequencer(ICharStateSequencer baseCss, int max, ENCharState state, IStatesCounter sc) {
		this.baseCss = baseCss;
		this.max = max;
		this.state = state;
		this.sc = sc; 
	}

	@Override
	public ENCharState nextState(ENCharState curState,
			ENCharState... forbidden) {
		ENCharState[] newForb = forbidden;
		if (sc.getStatesCount(state) >= max) {
			newForb = Arrays.copyOf(forbidden, forbidden.length + 1);
			newForb[newForb.length - 1] = state;
		}
		return baseCss.nextState(curState, newForb);
	}
}

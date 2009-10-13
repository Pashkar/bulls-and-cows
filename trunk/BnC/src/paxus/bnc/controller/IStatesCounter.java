package paxus.bnc.controller;

import paxus.bnc.model.ENCharState;

public interface IStatesCounter {
	public int getStatesCount(ENCharState state, Character ch, int pos);
}

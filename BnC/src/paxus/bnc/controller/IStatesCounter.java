package paxus.bnc.controller;

import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;

public interface IStatesCounter {
	public int getStatesCount(ENCharState state, Char ch, int pos);
}

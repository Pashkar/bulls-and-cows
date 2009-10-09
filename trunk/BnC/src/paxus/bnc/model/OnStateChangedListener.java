package paxus.bnc.model;

public interface OnStateChangedListener {

	public abstract void onStateChanged(Character ch, ENCharState newState);

}
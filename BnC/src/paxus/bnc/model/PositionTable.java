package paxus.bnc.model;

import java.util.ArrayList;
import java.util.HashMap;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateChangedListener;
import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.IPositionTableListener;
import paxus.bnc.controller.IStatesCounter;

public class PositionTable implements IStatesCounter, ICharStateChangedListener {

	public final ArrayList<PositionLine> lines = new ArrayList<PositionLine>(Run.MAX_WORD_LENGTH);
	
	//TODO replace Map with ordinal array by char -> int
	public final HashMap<Character, PositionLine> char2line = new HashMap<Character, PositionLine>(Run.MAX_WORD_LENGTH);
	
	private final ArrayList<IPositionTableListener> posTableListenerList = new ArrayList<IPositionTableListener>();
	
	private ICharStateSequencer css;
	public void setCss(ICharStateSequencer defaultCss) {
		this.css = defaultCss;
	}
	
	public final int maxLines;

	public final int wordLength;

	//package-private
	//for use from Run and tests
	PositionTable(int maxLines, int wordLength) {
		this.maxLines = maxLines;
		this.wordLength = wordLength;
	}
	
	public int addLine(Character ch) throws BncException {
		if (lines.size() == maxLines)
			throw new BncException("Too much lines to add");
		if (char2line.containsKey(ch))
			throw new BncException("Duplicate char");
		
		PositionLine line = new PositionLine(ch);
		lines.add(line);
		char2line.put(ch, line);
		notifyListeners(true, ch, line);
		return lines.size();
	}
	
	public int removeLine(Character ch) throws BncException {
		PositionLine line = char2line.remove(ch);
		if (line != null) {		//ignore invocation for already removed line
			lines.remove(line);
			notifyListeners(false, ch, null);
		}
		return lines.size();
	}
	
	private void notifyListeners(boolean insert, Character ch, PositionLine line) {
		for (IPositionTableListener listener : posTableListenerList) {
			listener.onPosTableUpdate(insert, ch, line);
		}
	}
	
	public void onCharStateChanged(Character ch, ENCharState newState) throws BncException {
		//automatically add/remove row on char marked/unmarked as PRESENT
		if (newState == ENCharState.PRESENT)
			addLine(ch);
		else 
			removeLine(ch);
	}
	
	public int getLinesCount() {
		return lines.size();
	}
	
	public void addStateChangedListener(IPositionTableListener listener) {
		posTableListenerList.add(listener);
	}
	
	public void removeStateChangedListener(IPositionTableListener listener) {
		posTableListenerList.remove(listener);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (PositionLine line : lines) {
			sb.append(line + "\n");
		}
		return sb.toString();
	}

	/**
	 * 	Only one {@link ENCharState.PRESENT} in line/column allowed. <br/>
	 *  All chars with {@link ENCharState.ABSENT} in line/column not allowed.
	 *  this.css must take care of that.
	 * @throws BncException 
	 */
	public ENCharState movePosStateForChar(Character ch, int pos) throws BncException {
		if (pos >= wordLength)
			throw new BncException("Pos value more than wordLength");
		PositionLine line = char2line.get(ch);
		PosChar pch = line.chars[pos];
		return movePosState(pch);
	}
	
	public ENCharState movePosState(PosChar pch) {
		return setPosCharState(pch, css.nextState(pch.state, pch.ch, pch.pos));
	}
	
	private ENCharState setPosCharState(PosChar pch, ENCharState newState) {
		ENCharState curState = pch.state;
		if (curState == newState) 
			return newState;
		pch.state = newState;
		pch.onPosStateChanged(pch, newState);	//notify exact PosChar
		return newState;
	}

	/**
	 * 	Only one {@link ENCharState.PRESENT} in line/column allowed. <br/>
	 *  All chars with {@link ENCharState.ABSENT} in line/column not allowed.
	 *  getStatesCount() designed to be used under LimitedStateSequencer
	 */
	public int getStatesCount(ENCharState state, Character ch, int pos) {
		final PositionLine line = char2line.get(ch);
		if (line == null)
			return 0;
		switch(state) {
		case PRESENT:
			return Math.max(line.getPresentCount(), getPresentCountInColumn(pos));
		case ABSENT:
			return Math.max(line.getAbsentCount(), getAbsentCountInColumn(pos));
		default:
			return 0;
		}
	}
	
	private Character getPresentInColumn(int pos) {
		for (PositionLine line : lines) {
			PosChar posChar = line.chars[pos];
			if (posChar.state == ENCharState.PRESENT)
				return posChar.ch;
		}
		return null;
	}
	
	private int getPresentCountInColumn(int pos) {
		return getPresentInColumn(pos) == null ? 0 : 1;
	}

	/*private Set<Char> getAbsentInColumn(int pos) {
		Set<Char> chars = new HashSet<Char>();
		for (PositionLine line : lines) {
			PosChar posChar = line.chars[pos];
			if (posChar.state == ENCharState.ABSENT)
				chars.add(posChar.ch);
		}
		return chars;
	}*/
	
	private int getAbsentCountInColumn(int pos) {
		int res = 0;
		for (PositionLine line : lines) {
			PosChar posChar = line.chars[pos];
			if (posChar.state == ENCharState.ABSENT)
				res++;
		}
		return res;
	}

	
	public final class PositionLine {

		public final PosChar[] chars = new PosChar[Run.MAX_WORD_LENGTH];
		
		public PositionLine(Character ch) {
			PosChar[] mChars = this.chars;
			for (int i = 0; i < wordLength; i++) {
				mChars[i] = new PosChar(ch, i, PositionTable.this);
			}
		}
		
		public int getPosPresent() {
			PosChar[] mChars = chars;
			for (int i = 0; i < wordLength; i++) {
				if (mChars[i].state == ENCharState.PRESENT)
					return i;
			}
			return -1;
		}
		
		public int getPresentCount() {
			return getPosPresent() == -1 ? 0 : 1;
		}
		
		public int getAbsentCount() {
			PosChar[] mChars = chars;
			int res = 0;
			for (int i = 0; i < wordLength; i++) {
				if (mChars[i].state == ENCharState.ABSENT)
					res++;
			}
			return res;
		}
		
/*		public Set<Integer> getPosAbsent() {
			PosChar[] mChars = chars;
			HashSet<Integer> res = new HashSet<Integer>();
			for (int i = 0; i < maxLines; i++) {
				if (mChars[i].state == ENCharState.ABSENT)
					res.add(i);
			}
			return res;
		}*/
		
		@Override
		public String toString() {
			PosChar[] mChars = chars;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < PositionTable.this.wordLength; i++) {
				sb.append(mChars[i]);
				sb.append(" ");
			}
			return sb.toString();
		}
	}
}

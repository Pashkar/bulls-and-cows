package paxus.bnc.model;

import java.util.ArrayList;
import java.util.HashMap;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.IStatesCounter;

public class PositionTable implements IStatesCounter {

	private final ArrayList<PositionLine> lines = new ArrayList<PositionLine>(Run.MAX_WORD_LENGTH);
	
	private final HashMap<Char, PositionLine> char2line = new HashMap<Char, PositionLine>(Run.MAX_WORD_LENGTH); 
	
	private ICharStateSequencer css;
	public void setCss(ICharStateSequencer defaultCss) {
		this.css = defaultCss;
	}
	
	public final int maxLines;
	
	public PositionTable(int maxLines) {
		this.maxLines = maxLines;
	}
	
	public int addLine(Char ch) throws BncException {
		if (lines.size() == maxLines)
			throw new BncException("Too much lines to add");
		if (char2line.containsKey(ch))
			throw new BncException("Duplicate char");
		
		PositionLine line = new PositionLine(ch);
		lines.add(line);
		char2line.put(ch, line);
		return lines.size();
	}
	
	public int removeLine(Char ch) throws BncException {
		PositionLine line = char2line.remove(ch);
		if (line == null)
			throw new BncException("No line for " + ch + " found");
		lines.remove(line);
		return lines.size();
	}
	
	public int getLinesCount() {
		return lines.size();
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
	 */
	public ENCharState moveStateForChar(Char ch, int pos) {
		PositionLine line = char2line.get(ch);
		PosChar pch = line.chars[pos];
		pch.state = css.nextState(pch.state, ch, pos);
		return pch.state;
	}
	
	/**
	 * 	Only one {@link ENCharState.PRESENT} in line/column allowed. <br/>
	 *  All chars with {@link ENCharState.ABSENT} in line/column not allowed.
	 *  getStatesCount() designed to be used under LimitedStateSequencer
	 */
	public int getStatesCount(ENCharState state, Char ch, int pos) {
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
	
	private Char getPresentInColumn(int pos) {
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

	
	private final class PositionLine {

		private final Char ch;	//to manipulate real state in alphabet

		private final PosChar[] chars = new PosChar[Run.MAX_WORD_LENGTH];
		
		public PositionLine(Char ch) {
			this.ch = ch;
			PosChar[] mChars = this.chars;
			for (int i = 0; i < PositionTable.this.maxLines; i++) {
				mChars[i] = new PosChar(ch);
			}
		}
		
		public int getPosPresent() {
			PosChar[] mChars = chars;
			for (int i = 0; i < maxLines; i++) {
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
			for (int i = 0; i < maxLines; i++) {
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
			sb.append(ch.asString + " ");
			for (int i = 0; i < PositionTable.this.maxLines; i++) {
				sb.append(mChars[i]);
			}
			return sb.toString();
		}
	}
	
	public final class PosChar {

		final Char ch;
		
		ENCharState state = ENCharState.NONE;	//to be manipulated in special way as it's not an ordinal Char 

		public PosChar(Char ch) {
			this.ch = ch;
		}

		@Override
		public String toString() {
			return state + ""; 
		}
	}
}
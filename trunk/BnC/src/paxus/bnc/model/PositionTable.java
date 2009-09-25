package paxus.bnc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;

public class PositionTable {

	private final ArrayList<PositionLine> lines = new ArrayList<PositionLine>(Run.MAX_WORD_LENGTH);

	private final HashMap<Char, PositionLine> char2line = new HashMap<Char, PositionLine>(Run.MAX_WORD_LENGTH); 
	
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
	 * 	Only one {@link ENCharState.PRESENT}  in line/column allowed. <br/>
	 *  All chars with {@link ENCharState.ABSENT} in line/column not allowed.
	 */
	public ENCharState moveStateForChar(Char ch, ICharStateSequencer css, int pos) {
		PositionLine line = char2line.get(ch);
		PosChar pch = line.chars[pos];
		
		boolean presentAllowed = line.getPosPresent() < 0 && this.getCharPresentInColumn(pos) == null; 
		boolean absentAllowed = line.getPosAbsent().size() < maxLines - 1 && this.getCharAbsentInColumn(pos).size() < maxLines - 1;
		
		//LimitedStateSequencer can be used, but simpler way used due to performance
		ENCharState newState = css.nextState(pch.state, 
				presentAllowed ? null : ENCharState.PRESENT,
				absentAllowed ? null : ENCharState.ABSENT);
		pch.state = newState;
		return newState;
	}

	private Char getCharPresentInColumn(int pos) {
		for (PositionLine line : lines) {
			PosChar posChar = line.chars[pos];
			if (posChar.state == ENCharState.PRESENT)
				return posChar.ch;
		}
		return null;
	}

	private Set<Char> getCharAbsentInColumn(int pos) {
		Set<Char> chars = new HashSet<Char>();
		for (PositionLine line : lines) {
			PosChar posChar = line.chars[pos];
			if (posChar.state == ENCharState.ABSENT)
				chars.add(posChar.ch);
		}
		return chars;
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
		
		public Set<Integer> getPosAbsent() {
			PosChar[] mChars = chars;
			HashSet<Integer> res = new HashSet<Integer>();
			for (int i = 0; i < maxLines; i++) {
				if (mChars[i].state == ENCharState.ABSENT)
					res.add(i);
			}
			return res;
		}
		
		@Override
		public String toString() {
			PosChar[] mChars = chars;
			StringBuilder sb = new StringBuilder();
			sb.append(ch.ch + " ");
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

package paxus.bnc.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.controller.IPositionTableListener;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.PositionTable.PositionLine;

public class PositionTableTest extends TestCase {

	public void testPositionTable() throws BncException {
		final Alphabet la = new Alphabet.Latin();
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(la, "abcd");
		PositionTable table = run.posTable;

		table.addLine(new Character('a'));
		
		//duplicate char
		boolean ok = false;
		try{ table.addLine(new Character('a'));}
		catch (BncException e) {ok = true;}
		assertTrue(ok);
		
		table.addLine(new Character('b'));
		table.addLine(new Character('c'));
		table.addLine(new Character('d'));

		//too much lines
		ok = false;
		try{ table.addLine(new Character('e'));}
		catch (BncException e) {ok = true;}
		assertTrue(ok);
		
		//remove wrong char - no erros, just ignores
		table.removeLine(new Character('z'));
		
		//too big pos value
		ok = false;
		try{ table.movePosStateForChar(new Character('z'), 10);}
		catch (BncException e) {ok = true;}
		assertTrue(ok);
		
		//change state to be sure that after following removing states are cleared
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(new Character('a'), 0));
		
		assertEquals(3, table.removeLine(new Character('a')));
		table.addLine(new Character('a'));
		assertEquals(4, table.getLinesCount());
		
		//all ABSENT in line not allowed
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(new Character('a'), 0));
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(new Character('a'), 1));
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(new Character('a'), 2));
		assertEquals(ENCharState.PRESENT, table.movePosStateForChar(new Character('a'), 3));
		assertEquals(ENCharState.NONE, table.movePosStateForChar(new Character('a'), 3));
		assertEquals(ENCharState.PRESENT, table.movePosStateForChar(new Character('a'), 3));
		
		//Only one PRESENT in column
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(new Character('b'), 3));
		assertEquals(ENCharState.NONE, table.movePosStateForChar(new Character('b'), 3));
		
		//If line with PRESENT removed, PRESENT in column allowed   
		table.removeLine(new Character('a'));
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(new Character('b'), 3));
		assertEquals(ENCharState.PRESENT, table.movePosStateForChar(new Character('b'), 3));
		
		//Only one PRESENT in line
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(new Character('b'), 0));
		assertEquals(ENCharState.NONE, table.movePosStateForChar(new Character('b'), 0));
		
		//If PRESENT in line removed, another PRESENT allowed
		assertEquals(ENCharState.NONE, table.movePosStateForChar(new Character('b'), 3));
		assertEquals(ENCharState.ABSENT, table.movePosStateForChar(new Character('b'), 0));
		assertEquals(ENCharState.PRESENT, table.movePosStateForChar(new Character('b'), 0));
	}
	
	public void testAllPosStateChangedListener() throws BncException {
		PositionTable table = new PositionTable(1, 3);
		table.setCss(ICharStateSequencer.FORWARD);
		Character charA = new Character('a');
		table.addLine(charA);
		final PosChar pchA0 = table.char2line.get(charA).chars[0];

		final int[] counter = {0};
		final IPosCharStateChangedListener listener = new IPosCharStateChangedListener() {
			public void onPosCharStateChanged(PosChar pch, ENCharState newState) {
				if (pch.ch.equals(new Character('a')) && pch.pos == 0)
					counter[0]++;
			}
		};
		
		//no listeners
		table.movePosStateForChar(charA, 0);
		assertEquals(0, counter[0]);
		
		table.addAllPosCharStateChangedListener(listener);
		
		//move by poschar
		pchA0.movePosState();
		assertEquals(1, counter[0]);
		
		//move by table
		table.movePosStateForChar(charA, 0);
		assertEquals(2, counter[0]);
		
		//another poschar
		table.movePosStateForChar(charA, 1);
		assertEquals(2, counter[0]);
	}

	public void testAddRemoveLine() throws BncException {
		final Alphabet da = new Alphabet.Digital();
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(da, "01");

		//check that all PRESENT chars are added as new lines to PosTable and removed when become not PRESENT

		//move by alphabet
		assertEquals(ENCharState.ABSENT, da.moveCharState(new Character('0')));
		assertEquals(0, run.posTable.getLinesCount());
		
		//move by Char
		assertEquals(ENCharState.PRESENT, Char.valueOf(new Character('0'), da).moveState());
		assertEquals(1, run.posTable.getLinesCount());
		
		assertEquals(ENCharState.PRESENT, da.moveCharState(new Character('1'), ENCharState.ABSENT));
		assertEquals(2, run.posTable.getLinesCount());
		
		//check that line removed
		assertEquals(ENCharState.NONE, da.moveCharState(new Character('0')));
		assertEquals(1, run.posTable.getLinesCount());
	}
	
	public void testPosTableListener() throws BncException {
		final Alphabet da = new Alphabet.Digital();
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(da, "0123");
		
		final int[] counter = {0, 0};
		final IPositionTableListener listener = new IPositionTableListener() {
			public void onPosTableUpdate(boolean insert, Character ch, PositionLine line) {
				counter[insert ? 0 : 1]++;
			}
		};
		
		//check w/o listeners
		run.posTable.addLine('0');
		assertEquals(0, counter[0]);
		assertEquals(0, counter[1]);
		assertEquals(0, run.posTable.removeLine('0'));
		assertEquals(0, counter[0]);
		assertEquals(0, counter[1]);
		
		//add line
		run.posTable.addPosTableListener(listener);
		run.posTable.addLine('1');
		assertEquals(1, counter[0]);
		assertEquals(0, counter[1]);
		
		//remove line
		assertEquals(0, run.posTable.removeLine('1'));
		assertEquals(1, counter[0]);
		assertEquals(1, counter[1]);
		
		//remove already removed line
		assertEquals(0, run.posTable.removeLine('1'));
		assertEquals(1, counter[0]);
		assertEquals(1, counter[1]);
		
		run.posTable.removeStateChangedListener(listener);
		run.posTable.addLine('0');
		assertEquals(1, counter[0]);
		assertEquals(1, counter[1]);
		assertEquals(0, run.posTable.removeLine('0'));
		assertEquals(1, counter[0]);
		assertEquals(1, counter[1]);
	}
	
	public void testAllPosListenerOnLineRemove() throws BncException {
		final Alphabet da = new Alphabet.Digital();
		RunExecutor re = new RunExecutor();
		Run run = re.startNewRun(da, "012");
		
		final int[] counter = {0, 0, 0};
		final IPosCharStateChangedListener charListener = new IPosCharStateChangedListener() {
			public void onPosCharStateChanged(PosChar pch, ENCharState newState) {
				counter[pch.ch - '0']++;
			}
		};
		
		//add line
		run.posTable.addLine('0');
		run.posTable.addLine('1');
		run.posTable.addLine('2');
		assertEquals(0, counter[0]);
		assertEquals(0, counter[1]);
		assertEquals(0, counter[2]);
		
		//mark positions
		run.posTable.movePosStateForChar('1', 0);
		run.posTable.movePosStateForChar('2', 0);
		run.posTable.movePosStateForChar('2', 1);
		
		//when line with pos marked removed listened on posChar must be notified 
		run.posTable.addAllPosCharStateChangedListener(charListener);

		//remove line '0' - no updates expected
		run.posTable.removeLine('0');
		run.posTable.removeLine('1');
		run.posTable.removeLine('2');
		assertEquals(0, counter[0]);
		assertEquals(1, counter[1]);
		assertEquals(2, counter[2]);
	}
	
	public void testSerialize() throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(baos);

		
		RunExecutor re = new RunExecutor();
		final Alphabet da = new Alphabet.Digital();
		Run run = re.startNewRun(da, "012");
		
		run.posTable.addLine('0');
		run.posTable.addLine('7');
		assertEquals(ENCharState.ABSENT, run.posTable.movePosStateForChar('7', 2));
		
		os.writeObject(run.posTable);
		os.close();
		
		ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		PositionTable t = (PositionTable) is.readObject();
		is.close();
		
		assertEquals(2, t.getLinesCount());
		PositionLine pl0 = t.char2line.get('0'); 
		assertEquals(ENCharState.NONE, pl0.chars[0].state);
		PositionLine pl7 = t.char2line.get('7');
		assertEquals(ENCharState.NONE, pl7.chars[0].state);
		assertEquals(ENCharState.ABSENT, pl7.chars[2].state);
	}
}

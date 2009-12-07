package paxus.bnc.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import paxus.bnc.BncException;
import paxus.bnc.controller.RunExecutor;

public class RunTest extends TestCase {

	public void testOfferWord() throws BncException {
		final Alphabet da = new Alphabet.Digital();
		RunExecutor re = new RunExecutor();
		re.startNewRun(da, "1234");
		re.offerWord("4321");
		WordComparisonResult res = re.offerWord("1234").result;
		assertEquals(2, re.run.wordsCompared.size());
		assertTrue(res.guessed());
	}
	
	public void testSerialize() throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(baos);
		
		final Alphabet da = new Alphabet.Digital();
		RunExecutor re = new RunExecutor();
		re.startNewRun(da, "1234");
		re.run.posTable.addLine('5');
		
		Word w = new Word(re.run.alphabet, "1243");
		WordComparisonResult comparisonResult = re.offerWord("1243").result;

		re.run.secretLine[1] = new Char(da, '5');
		
		os.writeObject(re.run);
		os.close();
		
		ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		Run run = (Run) is.readObject();
		is.close();
		
		assertEquals(4, run.wordLength);
		assertEquals("1234", run.secret.asString());
		assertEquals("Digital", run.alphabet.getName());
		assertEquals(1, run.posTable.getLinesCount());
		assertEquals(1, run.wordsCompared.size());
		assertEquals(comparisonResult.toString(), run.wordsCompared.get(0).result.toString());
		assertEquals(w.toString(), run.wordsCompared.get(0).word.toString());
		assertEquals(new Character('5'), run.secretLine[1].ch);
		assertEquals((Character)Char.NULL_CHAR, run.secretLine[0].ch);
	}
	
	public void testClearMarks() throws Exception {
		final Alphabet da = new Alphabet.Digital();
		RunExecutor re = new RunExecutor();
		re.startNewRun(da, "1234");
		assertEquals(ENCharState.PRESENT, da.moveCharState('1', ENCharState.ABSENT, ENCharState.NONE));
		assertEquals(ENCharState.ABSENT, da.moveCharState('2', ENCharState.PRESENT, ENCharState.NONE));
		assertEquals(1, re.run.posTable.getLinesCount());
		assertEquals(new Character('1'), re.run.posTable.lines.get(0).chars[0].ch);
		assertEquals(ENCharState.ABSENT, re.run.posTable.movePosStateForChar('1', 2));
		
		re.run.clearMarks();
		
		assertEquals(0, re.run.posTable.getLinesCount());
		assertEquals(ENCharState.NONE, da.getCharInstance('1').getState());
		assertEquals(ENCharState.NONE, da.getCharInstance('2').getState());
	}
}

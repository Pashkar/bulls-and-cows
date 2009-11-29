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
	}
}

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
		
		WordComparisonResult wcr = re.offerWord("1245").result;
		assertEquals(2, wcr.bullsCount);
		assertEquals(1, wcr.cowsCount);
	
		os.writeObject(re.run);
		os.close();
		System.out.println(this.getClass().getSimpleName() + "."+ getName() + ": " 
				+ baos.toByteArray().length + " bytes stored");
		
		ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		Run run = (Run) is.readObject();
		is.close();
		
		assertEquals(4, run.wordLength);
		assertEquals("1234", run.secret.asString());
		assertEquals("Digital", run.alphabet.getName());
		assertEquals(1, run.posTable.getLinesCount());
		assertEquals(1, run.wordsCompared.size());
		assertEquals("1245", run.wordsCompared.get(0).word.asString());
		assertEquals(2, run.wordsCompared.get(0).result.bullsCount);
		assertEquals(1, run.wordsCompared.get(0).result.cowsCount);
	}

}

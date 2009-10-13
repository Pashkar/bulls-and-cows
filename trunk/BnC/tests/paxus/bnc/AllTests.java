package paxus.bnc;

import junit.framework.Test;
import junit.framework.TestSuite;
import paxus.bnc.model.AlphabetTest;
import paxus.bnc.model.CharStateSequencerTest;
import paxus.bnc.model.CharTest;
import paxus.bnc.model.PosCharTest;
import paxus.bnc.model.PositionTableTest;
import paxus.bnc.model.RunExecutorTest;
import paxus.bnc.model.RunTest;
import paxus.bnc.model.WordComparisonResultTest;
import paxus.bnc.model.WordTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for paxus.bnc.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(CharTest.class);
		suite.addTestSuite(PosCharTest.class);
		suite.addTestSuite(AlphabetTest.class);
		suite.addTestSuite(RunTest.class);
		suite.addTestSuite(RunExecutorTest.class);
		suite.addTestSuite(WordComparisonResultTest.class);
		suite.addTestSuite(CharStateSequencerTest.class);
		suite.addTestSuite(PositionTableTest.class);
		suite.addTestSuite(WordTest.class);
		//$JUnit-END$
		return suite;
	}
}

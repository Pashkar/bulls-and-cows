package paxus.bnc;

import paxus.bnc.model.CharStateSequencerTest;
import paxus.bnc.model.PositionTableTest;
import paxus.bnc.model.RunTest;
import paxus.bnc.model.WordComparisonResultTest;
import paxus.bnc.model.WordTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for paxus.bnc.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(WordComparisonResultTest.class);
		suite.addTestSuite(PositionTableTest.class);
		suite.addTestSuite(RunTest.class);
		suite.addTestSuite(WordTest.class);
		suite.addTestSuite(CharStateSequencerTest.class);
		//$JUnit-END$
		return suite;
	}
}

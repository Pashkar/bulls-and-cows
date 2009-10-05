package paxus.bnc.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for paxus.bnc.model");
		//$JUnit-BEGIN$
		suite.addTestSuite(CharTest.class);
		suite.addTestSuite(RunTest.class);
		suite.addTestSuite(WordComparisonResultTest.class);
		suite.addTestSuite(CharStateSequencerTest.class);
		suite.addTestSuite(PositionTableTest.class);
		suite.addTestSuite(WordTest.class);
		//$JUnit-END$
		return suite;
	}

}

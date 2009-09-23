package paxus.bnc.android;

import paxus.bnc.BncException;
import paxus.bnc.controller.RunExecutor;
import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        RunExecutor re = new RunExecutor();
        try {
			re.startNewRun();
			re.offerWord("1243");
		} catch (BncException e) {
			e.printStackTrace();
		}
    }
}
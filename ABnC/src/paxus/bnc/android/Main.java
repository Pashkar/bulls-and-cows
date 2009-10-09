package paxus.bnc.android;

import paxus.bnc.BncException;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.Run;
import paxus.bnc.model.Word;
import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class Main extends Activity {
    private static final int COLUMNS = 9;
    private Alphabet alphabet;
	private final RunExecutor re = new RunExecutor();
	private Run run;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        try {
			startNewRun();
		} catch (BncException e) {}
		
		final Paint paint = createPaint();
		final LayoutInflater layoutInflater = getLayoutInflater();
		
        LinearLayout la = (LinearLayout) findViewById(R.id.DigitalAlphabetLayout);
        Char[] chars = alphabet.getAllChars().toArray(new Char[COLUMNS]);
		for (int i = 0; i < COLUMNS; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, null);		//is it possible just to "clone" CharView? - inflate involves xml parsing
        	cv.paint = paint;
        	cv.ch = chars[i];
        	la.addView(cv);
        }

        LinearLayout ls = (LinearLayout) findViewById(R.id.SecretLayout);
        chars = run.secret.chars;
        for (int i = 0; i < run.secret.wordLength; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, null);
        	cv.paint = paint;
        	cv.ch = chars[i];
        	ls.addView(cv);
        }
        
    }

	private Paint createPaint() {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(24);
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Align.CENTER);
        return paint;
	}
    
    private void startNewRun() throws BncException {
    	//TODO can keep alphabet instance if not changed and just reinit().
    	//alphabet.reinit();
    	
    	alphabet = new Alphabet.Digital();
    	//TODO offer alphabet selecting for user
    	
    	run = re.startNewRun(alphabet, "12345");
    }
    
}
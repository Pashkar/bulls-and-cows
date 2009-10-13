package paxus.bnc.android;

import paxus.bnc.BncException;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.Run;
import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class Main extends Activity {
    private static final int COLUMNS = 9;
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
		
        fillCharsLine((LinearLayout) findViewById(R.id.DigitalAlphabetLayout), 
        		run.alphabet.getAllChars().toArray(new Char[COLUMNS]), COLUMNS, layoutInflater, paint);
        fillCharsLine((LinearLayout) findViewById(R.id.SecretLayout), run.secret.chars, run.wordLength, layoutInflater, paint);
        
        LinearLayout pl = (LinearLayout) findViewById(R.id.PositioningLayout);
        for (int i = 0; i < run.wordLength; i++) {
        	LinearLayout la = new LinearLayout(this);
        	for (int j = 0; j < run.wordLength; j++) {
        		la.setOrientation(LinearLayout.HORIZONTAL);
        		PosCharView pcw = (PosCharView) layoutInflater.inflate(R.layout.poschar_view, null);
        		pcw.paint = paint;
        		la.addView(pcw);
        	}
        	pl.addView(la);
        }
        
    }

	private void fillCharsLine(LinearLayout la, Char[] chars, int length, final LayoutInflater layoutInflater, 
			final Paint paint) {
		for (int i = 0; i < length && i < COLUMNS; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, null);		//is it possible just to "clone" CharView? - inflate involves xml parsing
        	cv.paint = paint;
        	cv.setChar(chars[i]);
        	la.addView(cv);
        }
	}

	private Paint createPaint() {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(24);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(0xFFFFFFFF);
        return paint;
	}
    
    private void startNewRun() throws BncException {
    	//TODO can keep alphabet instance if not changed and just reinit().
    	//alphabet.reinit();
    	
    	//TODO offer alphabet selecting for user
    	
    	run = re.startNewRun(new Alphabet.Digital(), "12345");
    }
    
}
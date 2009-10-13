package paxus.bnc.android;

import paxus.bnc.BncException;
import paxus.bnc.android.view.CharView;
import paxus.bnc.android.view.PosCharView;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.PosChar;
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
	
	private final LinearLayout posLineLayout[] = new LinearLayout[Run.MAX_WORD_LENGTH];
	

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
        
        LinearLayout[] posLineLayout2 = posLineLayout;
        LinearLayout pl = (LinearLayout) findViewById(R.id.PositioningLayout);
        for (int i = 0; i < run.wordLength; i++) {
        	posLineLayout2[i] = new LinearLayout(this);
        	fillPosCharsLine(posLineLayout2[i], null, run.wordLength, layoutInflater, paint);
        	pl.addView(posLineLayout2[i]);
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
	
	private void fillPosCharsLine(LinearLayout la, PosChar[] posChars, int length, final LayoutInflater layoutInflater, 
			final Paint paint) {
		for (int i = 0; i < length && i < COLUMNS; i++) {
			PosCharView pcw = (PosCharView) layoutInflater.inflate(R.layout.poschar_view, null);		//is it possible just to "clone" CharView? - inflate involves xml parsing
			pcw.paint = paint;
			if (posChars != null)
				pcw.setPosChar(posChars[i]);
        	la.addView(pcw);
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
package paxus.bnc.android;

import java.util.LinkedList;

import paxus.bnc.BncException;
import paxus.bnc.android.view.CharView;
import paxus.bnc.android.view.PosCharView;
import paxus.bnc.controller.IPositionTableListener;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.PosChar;
import paxus.bnc.model.Run;
import paxus.bnc.model.PositionTable.PositionLine;
import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class Main extends Activity implements IPositionTableListener {
	
	//TODO update
    private static final int COLUMNS = 9;
    
	private final RunExecutor re = new RunExecutor();
	
	private Run run;
	
	private final LinkedList<LinearLayout> freePosLayoutList = new LinkedList<LinearLayout>();
	
	private LinearLayout posTableLayout;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        try {
			startNewRun();
		} catch (BncException e) {}
		
		Paint paint = createPaint();
		LayoutInflater layoutInflater = getLayoutInflater();
		
		//inflate alphabet layout
        inflateCharsLine((LinearLayout) findViewById(R.id.DigitalAlphabetLayout), 
        		run.alphabet.getAllChars().toArray(new Char[COLUMNS]), COLUMNS, layoutInflater, paint);
        
        //inflate secret word layout
        inflateCharsLine((LinearLayout) findViewById(R.id.SecretLayout), 
        		run.secret.chars, run.wordLength, layoutInflater, paint);
        
        
        //inflate all rows for PositionTable, store prepared lines in list for further usage
        LinkedList<LinearLayout> freePosLayoutList2 = freePosLayoutList;
        posTableLayout = (LinearLayout) findViewById(R.id.PositioningLayout);
        for (int i = 0; i < run.wordLength; i++) {
        	LinearLayout line = inflatePosLine(run.wordLength, layoutInflater, paint);
        	freePosLayoutList2.add(line);
        }
        
        run.posTable.addStateChangedListener(this);
    }

	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, final LayoutInflater layoutInflater, 
			final Paint paint) {
		for (int i = 0; i < length && i < COLUMNS; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, null);		//is it possible just to "clone" CharView? - inflate involves xml parsing
        	cv.paint = paint;
        	cv.setChar(chars[i]);
        	la.addView(cv);
        }
	}
	
	//inflate PosCharViews with PosChar.NULL values
	private LinearLayout inflatePosLine(int length, final LayoutInflater layoutInflater, final Paint paint) {
		LinearLayout line = new LinearLayout(this);
		for (int i = 0; i < length && i < COLUMNS; i++) {
			PosCharView pcw = (PosCharView) layoutInflater.inflate(R.layout.poschar_view, null);
			pcw.paint = paint;
        	line.addView(pcw);
        }
		return line;
	}
	
	//Already inflated LanearLayout, line of PosCharViews. Just associate PosChar objects
	private void showPosLine(LinearLayout pl, PosChar[] chars, int length) {
		for (int i = 0; i < length && i < COLUMNS; i++) {
			PosCharView pcw = (PosCharView) pl.getChildAt(i);
			pcw.setPosChar(chars[i]);
        }
		posTableLayout.addView(pl);
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

	public void onPosTableUpdate(boolean insert, Character ch, PositionLine line) {
		if (insert) {
			LinearLayout pl = freePosLayoutList.removeFirst();
			showPosLine(pl, line.chars, run.wordLength);
			pl.setTag(ch);
		} else {
			LinearLayout pl = removePosLine(ch);
			freePosLayoutList.add(pl);
			pl.setTag(null);
		}
	}

	private LinearLayout removePosLine(Character ch) {
		//clear PosCharViews
		LinearLayout line = (LinearLayout) posTableLayout.findViewWithTag(ch);
		for (int i = 0; i < run.wordLength; i++) {
			PosCharView pcv = (PosCharView) line.getChildAt(i);
			pcv.clearPosChar();
		}
		
		//move PosLineLayout down
		posTableLayout.removeView(line);
		
		return line;
	}
    
}
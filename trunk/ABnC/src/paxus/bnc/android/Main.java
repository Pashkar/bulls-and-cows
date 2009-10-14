package paxus.bnc.android;

import java.util.HashMap;

import paxus.bnc.BncException;
import paxus.bnc.android.view.CharView;
import paxus.bnc.android.view.PosCharView;
import paxus.bnc.controller.IPositionTableListener;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.PosChar;
import paxus.bnc.model.PositionTable;
import paxus.bnc.model.Run;
import paxus.bnc.model.PositionTable.PositionLine;
import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class Main extends Activity implements IPositionTableListener {
	
	//TODO update
    private static final int COLUMNS = 9;
    
	private final RunExecutor re = new RunExecutor();
	
	private Run run;
	
	//TODO replace explicit array with View.setId, -> findViewById
	private final LinearLayout posLineLayouts[] = new LinearLayout[Run.MAX_WORD_LENGTH];
	
	//FIXME improve!!!!
	//TODO replace explicit array with View.-> findViewWithTag
//	private final HashMap<Character, LinearLayout> char2posLineLayout = new HashMap<Character, LinearLayout>();
//	private final HashMap<Character, Integer> char2posLine = new HashMap<Character, LinearLayout>();

	private LayoutInflater layoutInflater;

	private Paint paint;

	private LinearLayout posTableLayout;
	

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        try {
			startNewRun();
		} catch (BncException e) {}
		
		paint = createPaint();
		layoutInflater = getLayoutInflater();
		
        inflateCharsLine((LinearLayout) findViewById(R.id.DigitalAlphabetLayout), 
        		run.alphabet.getAllChars().toArray(new Char[COLUMNS]), COLUMNS, layoutInflater, paint);
        inflateCharsLine((LinearLayout) findViewById(R.id.SecretLayout), run.secret.chars, run.wordLength, layoutInflater, paint);
        
        LinearLayout[] posLineLayout2 = posLineLayouts;
        posTableLayout = (LinearLayout) findViewById(R.id.PositioningLayout);
        for (int i = 0; i < run.wordLength; i++) {
        	posLineLayout2[i] = new LinearLayout(this);
        	posLineLayout2[i].setVisibility(View.INVISIBLE);
        	inflateEmptyPosLine(posLineLayout2[i], run.wordLength, layoutInflater, paint);
        	posTableLayout.addView(posLineLayout2[i]);
        }
        run.posTable.addStateChangedListener(this);
        
        
    }

	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, final LayoutInflater layoutInflater, 
			final Paint paint) {
		la.removeAllViews();
		for (int i = 0; i < length && i < COLUMNS; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, null);		//is it possible just to "clone" CharView? - inflate involves xml parsing
        	cv.paint = paint;
        	cv.setChar(chars[i]);
        	la.addView(cv);
        }
	}
	
	private void inflateEmptyPosLine(LinearLayout la, int length, final LayoutInflater layoutInflater, 
			final Paint paint) {
		la.removeAllViews();
		for (int i = 0; i < length && i < COLUMNS; i++) {
			PosCharView pcw = (PosCharView) layoutInflater.inflate(R.layout.poschar_view, null);
			pcw.paint = paint;
        	la.addView(pcw);
        }
	}
	
	//Already inflated LanearLayout, line of PosCharViews. Just associate PosChar objects
	private void showPosLine(LinearLayout pl, PosChar[] chars, int length) {
		for (int i = 0; i < length && i < COLUMNS; i++) {
			PosCharView pcw = (PosCharView) pl.getChildAt(i);
			pcw.setPosChar(chars[i]);
        }
		pl.setVisibility(View.VISIBLE);
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
		final PositionTable posTable = run.posTable;
		int linesCount = posTable.lines.size();
		if (insert) {
			LinearLayout pl = (LinearLayout) posTableLayout.getChildAt(linesCount);
			pl.setTag(ch);
			showPosLine(pl, posTable.lines.get(linesCount - 1).chars, run.wordLength);
		} else {
			LinearLayout pl = removePosLine(ch);
			pl.setTag(null);
		}
	}

	private LinearLayout removePosLine(Character ch) {
		//clear PosCharViews
		LinearLayout pll = (LinearLayout) posTableLayout.findViewWithTag(ch);
		for (int i = 0; i < run.wordLength; i++) {
			PosCharView pcv = (PosCharView) pll.getChildAt(i);
			pcv.clearPosChar();
		}
//		pll.setVisibility(View.INVISIBLE);
		
		
		//move PosLineLayout down
//		LinearLayout[] posLineLayouts2 = posLineLayouts;
		
		return pll;
	}
    
}
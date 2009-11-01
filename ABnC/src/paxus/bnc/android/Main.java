package paxus.bnc.android;

import java.util.LinkedList;

import paxus.bnc.BncException;
import paxus.bnc.android.view.CharLineLayout;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class Main extends Activity implements IPositionTableListener, OnClickListener {
	
	private final RunExecutor re = new RunExecutor();
	
	private Run run;
	
	private final LinkedList<LinearLayout> freePosLayoutList = new LinkedList<LinearLayout>();
	
	private LinearLayout posTableLayout;
	
	private LinearLayout offeredsLayout;
	
	private StringBuffer enteringWord = new StringBuffer();

	private LinearLayout enteringWordLayout;

	private Paint paint;

	private LayoutInflater layoutInflater;

	private Paint createPaint() {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(getResources().getColor(R.drawable.paint_color));
        return paint;
	}
    
    private void startNewRun() throws BncException {
    	//TODO can keep alphabet instance if not changed and just reinit().
    	//alphabet.reinit();
    	
    	//TODO offer alphabet selecting for user
    	
    	run = re.startNewRun(new Alphabet.Digital(), "12345");
    }
	
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
		
		enteringWordLayout = (LinearLayout)findViewById(R.id.EnteringLayout);
		Run run2 = run;
		//inflate entering word layout
		inflateCharsLine(enteringWordLayout, 
        		null, run2.wordLength , -1);
		
		//inflate alphabet layout
        inflateCharsLine((LinearLayout) findViewById(R.id.DigitalAlphabetLayout), 
        		run2.alphabet.getAllChars().toArray(new Char[10]), 10, R.id.AlphabetCharView);
        
        //inflate secret word layout
        inflateCharsLine((LinearLayout) findViewById(R.id.SecretLayout), 
        		run2.secret.chars, run2.wordLength, -1);
        
        //inflate all rows for PositionTable, store prepared lines in list for further usage
        LinkedList<LinearLayout> freePosLayoutList2 = freePosLayoutList;
        posTableLayout = (LinearLayout) findViewById(R.id.PositioningLayout);
        for (int i = 0; i < run2.wordLength; i++) {
        	LinearLayout line = inflatePosLine();
        	freePosLayoutList2.add(line);
        }
        
        run2.posTable.addStateChangedListener(this);
        
        offeredsLayout = (LinearLayout) findViewById(R.id.OfferedsLayout);
//        ScrollView scroll = (ScrollView) findViewById(R.id.ScrollOfferedsLayout);
    }

	public void onPosTableUpdate(boolean insert, Character ch, PositionLine line) {
		LinkedList<LinearLayout> freePosLayoutList2 = freePosLayoutList;
		if (insert) {
			LinearLayout pl = freePosLayoutList2.removeFirst();
			showPosLine(pl, line.chars, run.wordLength);
			pl.setTag(ch);
		} else {
			LinearLayout pl = removePosLine(ch);
			freePosLayoutList2.add(pl);
			pl.setTag(null);
		}
	}
	
	//Already inflated LanearLayout, line of PosCharViews. Just associate PosChar objects
	private void showPosLine(LinearLayout pl, PosChar[] chars, int length) {
		for (int i = 0; i < length; i++) {
			PosCharView pcw = (PosCharView) pl.getChildAt(i);
			pcw.setPosChar(chars[i]);
        }
		posTableLayout.addView(pl);
	}
	
	private LinearLayout removePosLine(Character ch) {
		LinearLayout line = (LinearLayout) posTableLayout.findViewWithTag(ch);
		//hide PosLineLayout
		posTableLayout.removeView(line);
		return line;
	}

	public void onClick(View v) {
		if (v.getId() == R.id.AlphabetCharView) {
			LinearLayout enteringWordLayout2 = enteringWordLayout;
			if (enteringWord.length() >= run.wordLength) {
				try {
					offerWord(enteringWord.toString());
				} catch (BncException e) {}
				
				//just remove underlying Char obj, reuse CharView. Member CharView.viewPos remains correct
				for (int i = 0; i < enteringWordLayout2.getChildCount(); i++) {
					((CharView)enteringWordLayout2.getChildAt(i)).resetChar();	 
				}
				enteringWordLayout2.invalidate();	//batch invalidate for entire layout at once
				enteringWord = new StringBuffer();
			}
			
			CharView cv = (CharView) v;
			Character ch = cv.getChar().ch;
			//duplicates are not allowed
			if (enteringWord.indexOf("" + ch) != -1)
				return;
	//TODO show warning
				
			enteringWord.append(ch);
			int curPos = enteringWord.length() - 1;
			CharView ecv = (CharView)enteringWordLayout2.getChildAt(curPos);
			//for newly added char PosTable may have already set position and no updates will be sent - force posMatched
			ecv.setChar(cv.getChar(), run.posTable.getPresentPos(ch) == curPos);
		}
	}

	private void offerWord(String offered) throws BncException {
		offeredsLayout.addView(inflateOfferedLine(offered.toCharArray()));
	}
	
	

	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, int viewId) {
		for (int i = 0; i < length; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, null);		//is it possible just to "clone" CharView? - inflate involves xml parsing
        	cv.paint = paint;
        	if (chars != null)
        		cv.setChar(chars[i]);
        	if (viewId != -1)
        		cv.setId(viewId);
        	if (viewId == R.id.AlphabetCharView)
        		cv.setOnClickListener(this);	//enter new word by clicks
        	if(la.getId() == R.id.EnteringLayout || 
        	   la.getId() == R.id.SecretLayout) {
        		cv.setViewPos(i);				//to mark "bull" in these words
        		run.posTable.addAllPosCharStateChangedListener(cv);		
        	}
        	la.addView(cv);
        }
	}
	
	//inflate PosCharViews with PosChar.NULL values
	private LinearLayout inflatePosLine() {
		LayoutInflater layoutInflater2 = layoutInflater;
		int wordLength = run.wordLength;
		CharLineLayout line = (CharLineLayout) layoutInflater2.inflate(R.layout.posline_view, posTableLayout, false);
		for (int i = 0; i < wordLength; i++) {
			PosCharView pcw = (PosCharView) layoutInflater2.inflate(R.layout.poschar_view, line, false);
			pcw.paint = paint;
			line.setWordLength(wordLength);
        	line.addView(pcw);
        }
		return line;
	}
	
	private LinearLayout inflateOfferedLine(char[] chars) throws BncException {
		LinearLayout line = new LinearLayout(this);		//TODO maybe inflate from xml?
		Run run2 = run;
		for (int i = 0; i < run2.wordLength; i++) {
			CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, null);
			cv.setChar(Char.valueOf(chars[i], run2.alphabet), run2.posTable.getPresentPos(chars[i]) == i);
			cv.setViewPos(i);
			cv.paint = paint;
        	run2.posTable.addAllPosCharStateChangedListener(cv);
			line.addView(cv);
        }
		return line;
	}

}
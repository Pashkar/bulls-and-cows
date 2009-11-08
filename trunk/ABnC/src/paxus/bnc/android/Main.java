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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Main extends Activity implements IPositionTableListener, OnClickListener {
	
	private final RunExecutor re = new RunExecutor();
	private Run run;
	private StringBuffer enteringWord = new StringBuffer();
	
	private LayoutInflater layoutInflater;
	private LinearLayout offeredsLayout;
	private LinearLayout enteringWordLayout;
	private LinearLayout posTableLayout;
	private final LinkedList<LinearLayout> freePosLayoutList = new LinkedList<LinearLayout>();

	private Paint paint;
	private Toast duplicateToast;
	private LayoutAnimationController lineInAnimation;
	private LayoutAnimationController lineOutAnimation;
	
	private Paint createPaint() {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(getResources().getColor(R.drawable.paint_color));
        return paint;
	}
	
	//TODO try android:persistentDrawingCache for frequent animation
	//TODO try android:drawingCacheQuality 
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.v("Main", "onCreate");
		
        setContentView(R.layout.main);
        
		paint = createPaint();
		layoutInflater = getLayoutInflater();
		duplicateToast = Toast.makeText(this, R.string.diplicated_msg, Toast.LENGTH_SHORT);
		lineInAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_random_fade_in);
		lineOutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_random_fade_out);
		
		try {
			startNewRun();
		} catch (BncException e) {}
		
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
    }
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.v("Main", "onSaveInstanceState");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v("Main", "onPause");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v("Main", "onResume");
	}

	private void startNewRun() throws BncException {
    	//TODO can keep alphabet instance if not changed and just reinit().
    	//alphabet.reinit();
    	
    	//TODO offer alphabet selecting for user
    	
    	run = re.startNewRun(new Alphabet.Digital(), "12345");
    }
    
	public void onPosTableUpdate(boolean insert, Character ch, PositionLine line) {
		LinkedList<LinearLayout> freePosLayoutList2 = freePosLayoutList;
		if (insert) {
			LinearLayout pl = freePosLayoutList2.removeFirst();
			showPosLine(pl, line.chars, run.wordLength);
			pl.setTag(ch);
		} else {
			LinearLayout pl = hidePosLine(ch);
			if (pl == null)
				return;
			freePosLayoutList2.add(pl);
			pl.setTag(null);
		}
	}
	
	//Already inflated LanearLayout, line of PosCharViews. Just associate PosChar objects
	private void showPosLine(LinearLayout line, PosChar[] chars, int length) {
		for (int i = 0; i < length; i++) {
			PosCharView pcw = (PosCharView) line.getChildAt(i);
			pcw.setPosChar(chars[i]);
        }
		posTableLayout.addView(line);
		line.setLayoutAnimation(lineInAnimation);
		line.invalidate(); //start layout animation	
	}
	
	//hide PosLineLayout - moved to CharLineLayout
	private LinearLayout hidePosLine(Character ch) {
		final LinearLayout line = (LinearLayout) posTableLayout.findViewWithTag(ch);
		if (line == null)
			return null;
		posTableLayout.removeView(line);
		
		//TODO - try to add transition animation - soft disappearing for row
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
			if (enteringWord.indexOf("" + ch) != -1) {
				duplicateToast.show();
				return;
			}
				
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
	
	
	//inflaters
	
	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, int viewId) {
		for (int i = 0; i < length; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, la, false);		//is it possible just to "clone" CharView? - inflate involves xml parsing
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
		LinearLayout line = (LinearLayout) layoutInflater2.inflate(R.layout.posline_view, posTableLayout, false);
		for (int i = 0; i < wordLength; i++) {
			PosCharView pcw = (PosCharView) layoutInflater2.inflate(R.layout.poschar_view, line, false);
			pcw.paint = paint;
        	line.addView(pcw);
        }
		return line;
	}
	
	private LinearLayout inflateOfferedLine(char[] chars) throws BncException {
		LayoutInflater layoutInflater2 = layoutInflater;
		LinearLayout line = (LinearLayout) layoutInflater2.inflate(R.layout.offeredline_view, offeredsLayout, false);
		Run run2 = run;
		for (int i = 0; i < run2.wordLength; i++) {
			CharView cv = (CharView) layoutInflater2.inflate(R.layout.char_view, line, false);
			cv.setChar(Char.valueOf(chars[i], run2.alphabet), run2.posTable.getPresentPos(chars[i]) == i);
			cv.setViewPos(i);
			cv.paint = paint;
        	run2.posTable.addAllPosCharStateChangedListener(cv);
			line.addView(cv);
        }
		return line;
	}

}
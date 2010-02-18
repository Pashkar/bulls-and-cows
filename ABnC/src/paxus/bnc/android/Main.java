package paxus.bnc.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import paxus.bnc.BncException;
import paxus.bnc.android.view.CharView;
import paxus.bnc.android.view.ComparisonResultView;
import paxus.bnc.android.view.PosCharView;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.controller.IPositionTableListener;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;
import paxus.bnc.model.PositionTable;
import paxus.bnc.model.Run;
import paxus.bnc.model.PositionTable.PositionLine;
import paxus.bnc.model.Run.WordCompared;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class Main extends Activity implements IPositionTableListener, OnClickListener, OnWordOfferedListener/*, ICharStateChangedListener, IPosCharStateChangedListener*/ {
	
	private static final String TAG = "Main";

	private static final String FNAME_PERSISTENCE = "persistence.dat";

	private final RunExecutor re = new RunExecutor();
	public static Run run;
	
	public static Paint paint;
	public static LayoutInflater layoutInflater;
	
	private EnteringPanel enteringPanel;
	private SecretWordPanel secretPanel;
	private LinearLayout offeredsLayout;
	private LinearLayout posTableLayout;
	private ScrollView scrollView;
	private final LinkedList<LinearLayout> freePosLayoutList = new LinkedList<LinearLayout>();

	private Toast alphabetNotSupportedToast;
//	private LayoutAnimationController lineInAnimation;
	private LinearLayout.LayoutParams charLP;
	private int displayWidth;

	private AlertDialog chooseAlphabetDialog;
	private AlertDialog chooseWordSizeDialog;
	private AlertDialog giveUpDialog;
	private AlertDialog clearMarksDialog;

	protected int wordSizeChosen;
	protected int alphabetChosen;

	private Button guessButton;


	private static Paint createPaint(Resources resources) {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(resources.getColor(R.drawable.paint_color));
        paint.setDither(true);
        return paint;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
        
		//init once per activity creation
		initActivity();
		initDialogs();
		
		run = null;
		try {
			run = restoreSavedRun();	//not null if restored
			re.run = run;
			reinitActivity();
			layoutViews();
		} 
		catch (Exception e) {
			Log.i(TAG, "restoreSavedState failed");
			startNewRun(false);	//invokes dialog chain, return null immediately
		}
    }

    private void initActivity() {
    	paint = createPaint(getResources());
//    	lineInAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_random_fade_in);
//    	lineOutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_random_fade_out);
    	layoutInflater = getLayoutInflater();
    	displayWidth = getWindowManager().getDefaultDisplay().getWidth();
    }
    
	private void reinitActivity() {
		setContentView(R.layout.main);
		
		offeredsLayout = (LinearLayout) findViewById(R.id.OfferedsLayout);
		posTableLayout = (LinearLayout) findViewById(R.id.PositioningLayout);
		scrollView = (ScrollView) findViewById(R.id.ScrollOffered);
		guessButton = (Button) findViewById(R.id.ShowAlphabetButton);
		guessButton.setEnabled(!run.givenUp);
		
		freePosLayoutList.clear();
		enteringPanel = new EnteringPanel(this, this);
	}

	/**
	 *	Asks on Alphabet and WodLength in chain:
	 *	@see Main#initDialogs()
	 *	Ends up with finishStartingNewRun():
	 *	@see Main#finishStartingNewRun()
	 */
	private void startNewRun(boolean cancellable) {
		Log.v(TAG, "startNewRun");
		chooseAlphabetDialog.setCancelable(cancellable);
		chooseWordSizeDialog.setCancelable(cancellable);
		chooseAlphabetDialog.show();	 
    }

	private void finishStartingNewRun() {
		Alphabet alphabet = new Alphabet.Digital();	
		int wordLength = wordSizeChosen;
		
		//TODO improve secret generating
		String secret = "12345";
		if (alphabet instanceof Alphabet.Digital) {
			secret = new String();
			Random rnd = new Random();
			Set<Character> secretSet = new HashSet<Character>(5);
			for (int i = 0; i < wordLength; i++) {
				Character c = new Character(String.valueOf(1 + rnd.nextInt(10)).charAt(0));
				while (secretSet.contains(c))	//no duplicates
					c = new Character(String.valueOf(1 + rnd.nextInt(10)).charAt(0));
				secretSet.add(c);
				secret += c;
			}
		}

		try {
			run = re.startNewRun(alphabet, secret);
		} catch (BncException e) {	}
		
//		Log.d(TAG, run.secret.toString());
		reinitActivity();
		layoutViews();	//finish initialization, interrupted by dialogs chain
	}
	
	private void initDialogs() {
		//Dialogs chain - Alphabet then WordLength
		chooseAlphabetDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.alphabet_title)
		.setItems(R.array.alphabet_array, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alphabetChosen = which;
				Log.i(TAG, "alphabet chosen: " + alphabetChosen);
				if (which >= 1) {
					if (alphabetNotSupportedToast == null)	//lazy init
						alphabetNotSupportedToast = Toast.makeText(Main.this, R.string.alphabet_not_supported_msg, 
								Toast.LENGTH_LONG);
					alphabetNotSupportedToast.show();
					return;
				}
				chooseWordSizeDialog.show();	//ask size in chain
			}
		})
		.create();
		
        chooseWordSizeDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.word_length_title)
        .setItems(R.array.word_length_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	wordSizeChosen = Run.MIN_WORD_LENGTH + which;
            	Log.i(TAG, "word size chosen: " + wordSizeChosen);
            	
            	finishStartingNewRun();
            }
        })
        .create();

        clearMarksDialog = new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.clear_marks_title)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Log.i(TAG, "Clear marks");
            	try {
					run.clearMarks();
				} catch (BncException e) {}
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		})
        .setMessage(R.string.clear_marks_conf)
        .create();
		
        giveUpDialog = new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.give_up_title)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Log.i(TAG, "Give up");
            	inflateAndShowAnswerDialog(R.string.secret_title, getResources().getString(R.string.give_up_msg));
            	guessButton.setEnabled(false);
            	re.giveUp();
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		})
        .setMessage(R.string.give_up_conf)
        .create();
        
        //TODO dialog with introduction
	}
	
	private void layoutViews() {
		Log.v(TAG, "initViews");
		Run run2 = run;
		PositionTable posTable = run2.posTable;
		
		((Button)findViewById(R.id.ShowAlphabetButton)).setOnClickListener(this);
		
		calcOfferedCharLayout();
		
        //inflate secret word layout
        LinearLayout secretLayout = (LinearLayout) findViewById(R.id.SecretLayout);
		inflateCharsLine(secretLayout, null, run2.wordLength, charLP);
		secretPanel = new SecretWordPanel(secretLayout);
        
        //inflate all rows for PositionTable, store prepared lines in list for further usage
		//also inflate labels
        LinkedList<LinearLayout> freePosLayoutList2 = freePosLayoutList;
        for (int i = 0; i < run2.wordLength; i++) {
        	LinearLayout line = inflatePosLine();
        	freePosLayoutList2.add(line);
        }
        posTable.addPosTableListener(this);
        
        //restore offered words
        try {
	        final List<WordCompared> wordsCompared = run2.wordsCompared;
			if (wordsCompared != null && wordsCompared.size() > 0) {
				Log.d(TAG, "restoring offered words: " + wordsCompared.size());
	        	for (WordCompared wc : wordsCompared)
					addOfferedWord(wc);
			}
		} catch (BncException e) {
			Log.e(TAG, "restore offered words failed", e);
		}
		
		//restore PosTable
		ArrayList<PositionLine> lines = posTable.lines;
		if (lines != null && lines.size() > 0)
        	for (PositionLine line : lines)
        		onPosTableUpdate(true, line.chars[0].ch, line);
	}

	//failed to work with "onMeasure" and "measureChildren" form API. Workaround proposed 
	private void calcOfferedCharLayout() {
		Resources resources = getResources();
		int width = displayWidth - (
				resources.getDimensionPixelSize(R.dimen.comp_result_right_margin) + 
				resources.getDimensionPixelSize(R.dimen.comp_result_width) +
				resources.getDimensionPixelSize(R.dimen.comp_result_left_margin) + 
				resources.getDimensionPixelSize(R.dimen.line_left_margin) +
				resources.getDimensionPixelSize(R.dimen.view_right_padding)
		);
		int charLeftMargin = resources.getDimensionPixelSize(R.dimen.char_left_margin);
		int charWidth = Math.min((width / run.wordLength) - charLeftMargin, resources.getDimensionPixelSize(R.dimen.char_width_max));
		Log.d(TAG, "calcOfferedCharLayout: charWidth = " + charWidth);
		charLP = new LinearLayout.LayoutParams(charWidth, resources.getDimensionPixelSize(R.dimen.char_height));
		charLP.leftMargin = charLeftMargin;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
		
		ObjectOutputStream oos = null;
		try {
			FileOutputStream fos = openFileOutput(FNAME_PERSISTENCE, MODE_PRIVATE);
			oos = new ObjectOutputStream(new BufferedOutputStream(fos));
			oos.writeObject(run);
		} catch (Exception e) {
			Log.e(TAG, "onPause", e);
		}
		finally {
			if (oos != null)
				try { oos.close(); } catch (IOException e) {}
		}
	}
	
	private Run restoreSavedRun() throws Exception {
		Log.v(TAG, "restoreSavedState");
		ObjectInputStream ois = null;
		Run run = null;
		try {
			FileInputStream fis = openFileInput(FNAME_PERSISTENCE);
			ois = new ObjectInputStream(new BufferedInputStream(fis));
			run = (Run) ois.readObject();
		}
		finally {
			if (ois != null)
				try { ois.close(); } catch (IOException e) {}
		}
		return run;
	}

	public void onPosTableUpdate(boolean insert, Character ch, PositionLine line) {
		if (insert) {
			LinearLayout pl = freePosLayoutList.removeFirst();
			pl.setTag(ch);
			showPosLine(pl, line.chars, run.wordLength);
		} else {				//line == null
			LinearLayout pl = hidePosLine(ch);
			if (pl == null)
				return;
			freePosLayoutList.add(pl);
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
//		line.setLayoutAnimation(lineInAnimation);
//		line.invalidate(); //start layout animation	
	}
	
	//hide PosLineLayout
	private LinearLayout hidePosLine(Character ch) {
		final LinearLayout line = (LinearLayout) posTableLayout.findViewWithTag(ch);
		if (line == null)
			return null;
		posTableLayout.removeView(line);
		
		//TODO - try to add transition animation - soft disappearing for row
		return line;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ShowAlphabetButton:
			enteringPanel.show();
			break;
		}
	}
	
	public void onWordOffered(String word) {
		Log.v(TAG, "word offered = " + word);
		if (word != null && word.length() == run.wordLength)
			offerWord(word);
	}
	
	private void offerWord(String word) {
		try {
			Run.WordCompared wc = re.offerWord(word);
			addOfferedWord(wc);
			scrollView.smoothScrollTo(0, 100000);
			Log.i(TAG, "offerWord = " + wc);
			if (wc.result.guessed())
				winGame(wc);
		} catch (BncException e) {}
	}
	 
	private void addOfferedWord(WordCompared wc) throws BncException {
		Log.d(TAG, "adding an offered word: " + wc);
		LinearLayout offeredLine = (LinearLayout) layoutInflater.inflate(R.layout.offeredline_layout, offeredsLayout, false);
		inflateCharsLine(offeredLine, wc.word.chars, run.wordLength, charLP);
		ComparisonResultView compResView = inflateComparisonResult(offeredLine, wc);
		offeredLine.addView(compResView);

		offeredsLayout.addView(offeredLine);
		offeredLine.setVisibility(View.VISIBLE);
	}

	private void winGame(WordCompared wc) {
    	Log.i(TAG, "Win game");
    	inflateAndShowAnswerDialog(R.string.win_title, getResources().getString(R.string.win_msg, run.wordsCompared.size())); 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.MenuNewGame:
				startNewRun(true);
				break;
			case R.id.MenuClearMarks:
				clearMarksDialog.show();
				break;
			case R.id.MenuGiveUp:
				giveUpDialog.show();
				break;				
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	
	///////////////////////////////////////////////////////////
	//Inflaters
	///////////////////////////////////////////////////////////
	
	private ComparisonResultView inflateComparisonResult(LinearLayout la, WordCompared wc) {
		ComparisonResultView compResView = (ComparisonResultView) layoutInflater.inflate(R.layout.comp_result_view, la, false);
		compResView.setPaint(paint);
		compResView.setResult(wc.result);
		return compResView;
	}

	private void inflateAndShowAnswerDialog(int titleId, String message) {
		LinearLayout answerLine = (LinearLayout) layoutInflater.inflate(R.layout.answerline_layout, null, false);
		inflateCharsLine(answerLine, run.secret.chars, run.wordLength, null);

		new AlertDialog.Builder(Main.this)
            	.setIcon(android.R.drawable.ic_dialog_alert)
            	.setTitle(titleId)
            	.setView(answerLine)
            	.setMessage(message)
        		.setPositiveButton(android.R.string.ok, null)
        		.show();
	}
	
	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, LayoutParams lp) {
		LayoutParams answerLP = null;
		if (la.getId() == R.id.AnswerLine) {
			answerLP = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.entering_char_width), 
						getResources().getDimensionPixelSize(R.dimen.entering_char_width));
    	}
	
		for (int i = 0; i < length; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, la, false);
        	cv.paint = paint;
        	if (chars != null && i < chars.length)
        		cv.setChar(chars[i]);
        	if (lp != null)
        		cv.setLayoutParams(lp);
        	
        	//to mark "bull" in these words
    		cv.setViewPos(i);
    		run.posTable.addAllPosCharStateChangedListener(cv);		
    		if (chars != null) {
	    		int presentPos = run.posTable.getPresentPos(chars[i].ch);
	   			cv.setChar(chars[i], i == presentPos);	//mark as "bull" at start 
    		}

    		if (la.getId() == R.id.AnswerLine) {
        		cv.changeStateOnClick = false;
        		cv.setLayoutParams(answerLP);
        	}
        	la.addView(cv);
        	cv.setVisibility(View.VISIBLE);
        }
	}
	
	//inflate PosCharViews with PosChar.NULL values
	private LinearLayout inflatePosLine() {
		LayoutInflater layoutInflater2 = layoutInflater;
		int wordLength = run.wordLength;
		LinearLayout line = (LinearLayout) layoutInflater2.inflate(R.layout.posline_layout, posTableLayout, false);
		
		for (int i = 0; i < wordLength; i++) {
			PosCharView pcw = (PosCharView) layoutInflater2.inflate(R.layout.poschar_view, line, false);
			pcw.setLayoutParams(charLP);
			pcw.paint = paint;
        	line.addView(pcw);
        }
		return line;
	}

	private class SecretWordPanel implements IPosCharStateChangedListener {
		
		public LinearLayout layout;
		private Run run2;

		public SecretWordPanel(LinearLayout layout) {
			this.layout = layout;
			run2 = run;
			
			//restore secret line serialized by Run object
			for (int i = 0; i < run2.wordLength; i++) {
				Char ch = run2.secretLine[i];
				CharView cv = (CharView)layout.getChildAt(i);
//				cv.setLayoutParams(linearCharLP);
				cv.setChar(ch, i == run2.posTable.getPresentPos(ch.ch));
				cv.changeStateOnClick = false;	//secret word should not react on clicks, just listen to changes produced by others
			}
			run2.posTable.addAllPosCharStateChangedListener(this);
		}

		public void onPosCharStateChanged(PosChar pch, ENCharState newState) {
			CharView cv = (CharView) layout.getChildAt(pch.pos);	//TODO replace with array
			final Char[] secretLine = run2.secretLine;
			if (newState == ENCharState.PRESENT) {
				try { 
					final Char ch = Char.valueOf(pch.ch, run2.alphabet);
					cv.setChar(ch, true);
					secretLine[pch.pos] = ch;
				} catch (BncException e) {}
			} else {
				final Char ch = cv.getChar();
				final Char nullChar = Char.NULL;
				if (pch.ch == ch.ch && ch != nullChar) {
					cv.resetChar();
					secretLine[pch.pos] = nullChar;
				}
			}
		}
	}
}
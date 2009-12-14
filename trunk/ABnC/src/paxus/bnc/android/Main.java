package paxus.bnc.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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
import android.content.Intent;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class Main extends Activity implements IPositionTableListener, OnClickListener {
	
	private static final String FNAME_PERSISTENCE = "persistence.dat";

	private final RunExecutor re = new RunExecutor();
	public static Run run;
	
	private LayoutInflater layoutInflater;
	private LinearLayout offeredsLayout;
	private LinearLayout posTableLayout;
	private SecretWordLayoutWrapper secretLayout;
	private final LinkedList<LinearLayout> freePosLayoutList = new LinkedList<LinearLayout>();

	private Paint paint;
	private LayoutAnimationController lineInAnimation;
	private LayoutAnimationController lineOutAnimation;
	private ScrollView scrollView;

	private AlertDialog clearMarksDialog;
	private AlertDialog newGameDialog;
	private AlertDialog wordSizeDialog;
	private AlertDialog alphabetDialog;

	protected int wordSizeChosen;
	protected int alphabetChosen;

	private AlertDialog giveUpDialog;
	
	public static Paint createPaint(Resources resources) {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(resources.getColor(R.drawable.paint_color));
        return paint;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.v("Main", "onCreate");
        
		initActivity();
    }

	private void initActivity() {
		setContentView(R.layout.main);
		paint = createPaint(getResources());
		layoutInflater = getLayoutInflater();
		lineInAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_random_fade_in);
		lineOutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_random_fade_out);
		offeredsLayout = (LinearLayout) findViewById(R.id.OfferedsLayout);	//TODO replace with GridLayout?
		posTableLayout = (LinearLayout) findViewById(R.id.PositioningLayout);
		scrollView = (ScrollView) findViewById(R.id.ScrollOfferedsLayout);
		freePosLayoutList.clear();

		//TODO separate layouts for portrait and landscape orientations
/*		orientationListener = new OrientationEventListener(this) {
			public void onOrientationChanged(int orientation) {
				Log.v("Main", "onOrientationChanged");
			}
		};
		orientationListener.enable();*/
		
		initDialogs();
		
		Run run = initRun();
		
		if (run != null)	//no dialogs, restored saved run
			initViews();
	}

	private void initDialogs() {
		clearMarksDialog = new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.clear_marks)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Log.i("Main", "Clear marks");
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
        .setTitle(R.string.give_up)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Log.i("Main", "Give up");
            	new AlertDialog.Builder(Main.this)
                		.setMessage("The word was: \n" + run.secret.asString().toUpperCase())
                		.show();
            	
            	//TODO disable game
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		})
        .setMessage(R.string.give_up_conf)
        .create();
		
		newGameDialog = new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.new_game)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Log.i("Main", "Start new game");
				File savedStateFile = getFileStreamPath(FNAME_PERSISTENCE);
				if (savedStateFile != null && savedStateFile.exists())
					savedStateFile.delete();
				initActivity();
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		})
        .setMessage(R.string.new_game_conf)
        .create();
		
		
		//Dialogs chain - Alphabet then WordLength
		alphabetDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.alphabet)
		.setItems(R.array.alphabet_array, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alphabetChosen = which;
				Log.i("Main", "alphabet chosen: " + alphabetChosen);
				
				wordSizeDialog.show();	//ask size in chain
			}
		})
//		.setCancelable(false)
		.create();
		
        wordSizeDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.word_length)
        .setItems(R.array.word_length_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	wordSizeChosen = Run.MIN_WORD_LENGTH + which;
            	Log.i("Main", "word size chosen: " + wordSizeChosen);
            	
            	finishStartingNewRun();
            }
        })
//        .setCancelable(false)
        .create();

        
        //TODO dialog with introduction
	}

	private Run initRun() {
		try {
			run = null;
			run = restoreSavedRun();	//not null if restored 
			re.run = run;
			return run;
		} 
		catch (Exception e) {
			Log.i("Main", "restoreSavedState failed");
		}
		
		//if failed to restore saved
		try { 
			startNewRun();	//invokes dialog chain, return null immediately
		} 
		catch (BncException e1) { Log.e("Main", "startNewRun failed", e1); }
		return null;
	}

	private void initViews() {
		Log.v("Main", "initViews");
		Run run2 = run;
		PositionTable posTable = run2.posTable;
		
		((Button)findViewById(R.id.ShowAlphabetButton)).setOnClickListener(this);
		
        //inflate secret word layout
        LinearLayout layout = (LinearLayout) findViewById(R.id.SecretLayout);
		inflateCharsLine(layout, null, run2.wordLength, -1);
		secretLayout = new SecretWordLayoutWrapper(layout);
        
        //inflate all rows for PositionTable, store prepared lines in list for further usage
        LinkedList<LinearLayout> freePosLayoutList2 = freePosLayoutList;
        for (int i = 0; i < run2.wordLength; i++) {
        	LinearLayout line = inflatePosLine();
        	freePosLayoutList2.add(line);
        }
        posTable.addPosTableListener(this);
        
        //restore offered words
        try {
	        final List<WordCompared> wordsCompared = run2.wordsCompared;
			if (wordsCompared != null && wordsCompared.size() > 0)
	        	for (WordCompared wc : wordsCompared)
						addOfferedWord(wc);
		} catch (BncException e) {
			Log.e("Main", "restore offered words failed", e);
		}
		
		//restore PosTable
		ArrayList<PositionLine> lines = posTable.lines;
		if (lines != null && lines.size() > 0)
        	for (PositionLine line : lines)
        		onPosTableUpdate(true, line.chars[0].ch, line); 
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v("Main", "onPause");
		
		ObjectOutputStream oos = null;
		try {
			FileOutputStream fos = openFileOutput(FNAME_PERSISTENCE, MODE_PRIVATE);
			oos = new ObjectOutputStream(new BufferedOutputStream(fos));
			oos.writeObject(run);
		} catch (Exception e) {
			Log.e("Main", "onPause", e);
		}
		finally {
			if (oos != null)
				try { oos.close(); } catch (IOException e) {}
		}
	}
	
	private Run restoreSavedRun() throws Exception {
		Log.v("Main", "restoreSavedState");
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
	
	private void startNewRun() throws BncException {
		Log.v("Main", "startNewRun");

		alphabetDialog.show();	//ask on Alphabet and WodLength in chain
		
		//will continue by dialogs chain
    }

	private Run finishStartingNewRun() {
		Alphabet alphabet = new Alphabet.Digital();	//TODO add other alphabets based on alphabetChosen
		int wordLength = wordSizeChosen;
		
		//TODO improve secret generating
		String secret = "12345";
		if (alphabet instanceof Alphabet.Digital) {
			secret = new String();
			Random rnd = new Random();
			Set<Character> secretSet = new HashSet<Character>(5);
			for (int i = 0; i < wordLength; i++) {
				Character c = new Character(String.valueOf(1 + rnd.nextInt(10)).charAt(0));
				while (secretSet.contains(c))
					c = new Character(String.valueOf(1 + rnd.nextInt(10)).charAt(0));
				secretSet.add(c);
				secret += c;
			}
		}

		try {
		
			run = re.startNewRun(alphabet, secret);

		} catch (BncException e) {
		}
		
		initViews();	//finish initialization, interrupted by dialogs chain
		
		return null;
	}

	public void onPosTableUpdate(boolean insert, Character ch, PositionLine line) {
		LinkedList<LinearLayout> freePosLayoutList2 = freePosLayoutList;
		if (insert) {
			LinearLayout pl = freePosLayoutList2.removeFirst();
			showPosLine(pl, line.chars, run.wordLength);
			pl.setTag(ch);
		} else {				//line == null
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
			Intent intent = new Intent(this, DigitalAlphabetActivity.class);
            startActivityForResult(intent, RESULT_FIRST_USER + 1);	//doesn't matter what code to use
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v("Main", "onActivityResult returned " + resultCode);
		switch (resultCode) {
			case RESULT_CANCELED:
				//nothing to do
				break;
			case RESULT_OK:
				String wordOffered = data.getAction();
				Log.i("Main", "alphabetActivity returned \"" + wordOffered + "\"");
				if (wordOffered != null && wordOffered.length() == run.wordLength)
					offerWord(wordOffered);
				break;
		}
	}

	private void offerWord(String word) {
		try {
			Run.WordCompared wc = re.offerWord(word);
			addOfferedWord(wc);
			scrollView.smoothScrollTo(0, 100000);
			Log.i("Main", "offerWord = " + wc);
			if (wc.result.guessed())
				winGame(wc);
		} catch (BncException e) {}
	}

	private void winGame(WordCompared wc) {
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.win_msg)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        })
        .setMessage("Congratulations! You guessed the word \"" + run.secret.asString() + "\" in ??? steps")
        .create()
        .show();
	}
 
	private void addOfferedWord(WordCompared wc) throws BncException {
		offeredsLayout.addView(inflateOfferedLine(wc));
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.MenuNewGame:
				newGameDialog.show();
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
	
	
	///////////////////////////////////////////////////////////
	//Inflaters
	///////////////////////////////////////////////////////////
	
	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, int viewId) {
		for (int i = 0; i < length; i++) {
        	CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, la, false);
        	cv.paint = paint;
        	if (chars != null)
        		cv.setChar(chars[i]);
        	if (viewId != -1)
        		cv.setId(viewId);
        	if(la.getId() == R.id.SecretLayout) {
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
	
	private LinearLayout inflateOfferedLine(WordCompared wc) throws BncException {
		final LayoutInflater layoutInflater2 = layoutInflater;
		LinearLayout line = (LinearLayout) layoutInflater2.inflate(R.layout.offered_line_view, offeredsLayout, false);
		Run run2 = run;
		char[] chars = wc.word.asString().toCharArray();
		for (int i = 0; i < run2.wordLength; i++) {
			CharView cv = (CharView) layoutInflater2.inflate(R.layout.char_view, line, false);
			cv.setChar(Char.valueOf(chars[i], run2.alphabet), run2.posTable.getPresentPos(chars[i]) == i);
			cv.setViewPos(i);
			cv.paint = paint;
        	run2.posTable.addAllPosCharStateChangedListener(cv);
			line.addView(cv);
		}
		ComparisonResultView crv = (ComparisonResultView) layoutInflater2.inflate(R.layout.comp_result_view, line, false);
		crv.setResult(wc.result);
		crv.setPaint(paint);
		line.addView(crv);
		return line;
	}
	
	private class SecretWordLayoutWrapper implements IPosCharStateChangedListener {
		
		public LinearLayout layout;
		private Run run2;

		public SecretWordLayoutWrapper(LinearLayout layout) {
			this.layout = layout;
			run2 = run;
			
			//restore secret line serialized by Run object
			for (int i = 0; i < run2.wordLength; i++) {
				Char ch = run2.secretLine[i];
				CharView cv = (CharView)layout.getChildAt(i);
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
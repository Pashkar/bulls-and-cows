package paxus.bnc.android;

import java.io.*;
import java.util.*;

import paxus.bnc.BncException;
import paxus.bnc.android.view.CharView;
import paxus.bnc.android.view.ComparisonResultView;
import paxus.bnc.android.view.PosCharView;
import paxus.bnc.controller.ICharStateChangedListener;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.controller.IPositionTableListener;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.*;
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
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

public class Main extends Activity implements IPositionTableListener, OnClickListener, OnWordOfferedListener, ICharStateChangedListener, IPosCharStateChangedListener, OnKeyListener {
	
	private static final String TAG = "Main";

	private static final String FNAME_PERSISTENCE = "persistence.dat";

	private final RunExecutor re = new RunExecutor();
	public static Run run;
	
	public static Paint paint;
	public static LayoutInflater layoutInflater;
	
	private EnteringPanel enteringPanel;
	private SecretWordPanel secretPanel;
	private GridView offeredsGrid;
	private CharLineAdapter offeredsAdapter;
	private LinearLayout posTableLayout;
	private final LinkedList<LinearLayout> freePosLayoutList = new LinkedList<LinearLayout>();

	private LayoutAnimationController lineInAnimation;
	private LayoutAnimationController lineOutAnimation;

	private AlertDialog chooseAlphabetDialog;
	private AlertDialog chooseWordSizeDialog;
	private AlertDialog giveUpDialog;
	private AlertDialog clearMarksDialog;

	protected int wordSizeChosen;
	protected int alphabetChosen;


	private Button guessButton;

	private int charWidth;
	private int charHeight;

	private LinearLayout.LayoutParams linearCharLP;

	private static Paint createPaint(Resources resources) {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16);
//        paint.setFakeBoldText(true);
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
    	lineInAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_random_fade_in);
    	lineOutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_random_fade_out);
    	layoutInflater = getLayoutInflater();
    	charHeight = getResources().getDimensionPixelSize(R.dimen.char_height);
    }
    
	private void reinitActivity() {
		charWidth = -1;
		setContentView(R.layout.main);
		posTableLayout = (LinearLayout) findViewById(R.id.PositioningLayout);
		guessButton = (Button) findViewById(R.id.ShowAlphabetButton);
		guessButton.setEnabled(!run.givenUp);
		
		freePosLayoutList.clear();
		linearCharLP = new LinearLayout.LayoutParams(getCharWidthInPx(), charHeight);
		linearCharLP.leftMargin = getResources().getDimensionPixelSize(R.dimen.char_margin_left);

		enteringPanel = new EnteringPanel(this, this);
		offeredsAdapter = new CharLineAdapter();
		offeredsGrid = (GridView) findViewById(R.id.OfferedsGrid);
		offeredsGrid.setNumColumns(run.wordLength + 1);
		offeredsGrid.setOnKeyListener(this);
		offeredsGrid.setAdapter(offeredsAdapter);
		
		run.alphabet.addAllCharsStateChangedListener(this);
		run.posTable.addAllPosCharStateChangedListener(this);
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
				while (secretSet.contains(c))	//no duplicates
					c = new Character(String.valueOf(1 + rnd.nextInt(10)).charAt(0));
				secretSet.add(c);
				secret += c;
			}
		}

		try {
			run = re.startNewRun(alphabet, secret);
		} catch (BncException e) {	}
		
		reinitActivity();
		layoutViews();	//finish initialization, interrupted by dialogs chain
	}
	
	private void initDialogs() {
		//Dialogs chain - Alphabet then WordLength
		chooseAlphabetDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.alphabet)
		.setItems(R.array.alphabet_array, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alphabetChosen = which;
				Log.i(TAG, "alphabet chosen: " + alphabetChosen);
				
				chooseWordSizeDialog.show();	//ask size in chain
			}
		})
		.create();
		
        chooseWordSizeDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.word_length)
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
        .setTitle(R.string.clear_marks)
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
        .setTitle(R.string.give_up)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Log.i(TAG, "Give up");
            	String str = run.secret.asString().toUpperCase();
            	StringBuffer sb = new StringBuffer(str.length() * 2);
            	for (char ch : str.toCharArray())
            		sb.append(ch).append(' ');
				new AlertDialog.Builder(Main.this)
		            	.setIcon(android.R.drawable.ic_dialog_alert)
		            	.setTitle(R.string.give_up)
		        		.setMessage("The secret was: \n\n\t" + sb.toString() + "\n")
		        		.setPositiveButton(android.R.string.ok, null)
		        		.show();
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
		
        //inflate secret word layout
        LinearLayout layout = (LinearLayout) findViewById(R.id.SecretLayout);
		inflateCharsLine(layout, null, run2.wordLength, -1);
		secretPanel = new SecretWordPanel(layout);
        
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
			Log.e(TAG, "restore offered words failed", e);
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
//			scrollView.smoothScrollTo(0, 100000);
//			offeredsGrid.scrollTo(0, 100000);
			Log.i(TAG, "offerWord = " + wc);
			if (wc.result.guessed())
				winGame(wc);
		} catch (BncException e) {}
	}
	 
	private void addOfferedWord(WordCompared wc) throws BncException {
//		offeredsLayout.addView(inflateOfferedLine(wc));
		offeredsAdapter.addWord(wc);
		offeredsAdapter.notifyDataSetChanged();
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
	

	public void onCharStateChanged(Character ch, ENCharState newState)
			throws BncException {
//		offeredsAdapter.notifyDataSetInvalidated();
		offeredsAdapter.notifyDataSetChanged();
	}

	public void onPosCharStateChanged(PosChar ch, ENCharState newState) {
//		offeredsAdapter.notifyDataSetInvalidated();
		offeredsAdapter.notifyDataSetChanged();
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
			pcw.setLayoutParams(linearCharLP);
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
				cv.setLayoutParams(linearCharLP);
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
	
	public class CharLineAdapter extends BaseAdapter {

		private ArrayList<CharView[]> charLines = new ArrayList<CharView[]>();
		private ArrayList<ComparisonResultView> results = new ArrayList<ComparisonResultView>();
		private GridView.LayoutParams gridCharLP = new GridView.LayoutParams(getCharWidthInPx(), charHeight);
		
		public void addWord(WordCompared wc) throws BncException {
			Log.i(TAG, "CharLineAdapter.addWord, wc = " + wc);
			Log.d(TAG, "charLines.size = " + charLines.size() + ", results.size = " + results.size());
			CharView[] line = new CharView[run.wordLength];
			char[] chars = wc.word.asString().toCharArray();
			for (int i = 0; i < run.wordLength; i++) {
				CharView cv = (CharView) layoutInflater.inflate(R.layout.char_view, offeredsGrid, false);
				cv.setChar(Char.valueOf(chars[i], run.alphabet), run.posTable.getPresentPos(chars[i]) == i);
				cv.setViewPos(i);
				cv.paint = paint;
	        	run.posTable.addAllPosCharStateChangedListener(cv);
				line[i] = cv;
			}
			charLines.add(line);
			
			ComparisonResultView crv = (ComparisonResultView) layoutInflater.inflate(R.layout.comp_result_view, offeredsGrid, false);
			crv.setResult(wc.result);
			crv.setPaint(paint);
			results.add(crv);
		}
		
		public int getCount() {
			return charLines.size() * (run.wordLength + 1);
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			int wordLength = run.wordLength;
			int lineNum = position / (wordLength + 1);
			int colNum = position % (wordLength + 1);
			if (colNum == wordLength)	//comparison result
				return results.get(lineNum);
			else {
				CharView cv = charLines.get(lineNum)[colNum];
				cv.setLayoutParams(gridCharLP);
				return cv;
			}
		}
	}
	
	public int getCharWidthInPx() {
		if (charWidth == -1)
			charWidth = getResources().getIntArray(R.array.char_width_array)[run.wordLength];
		return charWidth;
	}
	
	//GridView
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Log.v(TAG, this + ": onKey, event = " + event + ", keyCode = " + keyCode);
		/*if (event.getAction() == KeyEvent.ACTION_UP &&
				(keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
			Log.d(TAG, "Enter pressed for view " + v);
			if (v instanceof CharView) {
				((CharView) v).onClick(v);
			}
		}*/
		return false;
	}
}
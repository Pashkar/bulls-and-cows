package paxus.bnc.android;

import java.io.*;
import java.util.*;

import paxus.bnc.BncException;
import paxus.bnc.android.view.CharView;
import paxus.bnc.android.view.ComparisonResultView;
import paxus.bnc.android.view.PosCharView;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.controller.IPositionTableListener;
import paxus.bnc.controller.RunExecutor;
import paxus.bnc.model.*;
import paxus.bnc.model.PositionTable.PositionLine;
import paxus.bnc.model.Run.WordCompared;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class Main extends Activity implements IPositionTableListener, OnClickListener, OnWordOfferedListener {
	
	private static final String TAG = "Main";

	private static final String PERSISTENCE = "persistence.dat";
	
	private static final String DICT_LATIN_4 = "latin_dict_4.txt";
	private static final String DICT_LATIN_5 = "latin_dict_5.txt";
	private static final String DICT_LATIN_6 = "latin_dict_6.txt";

	private final RunExecutor re = new RunExecutor();
	public static Run run;
	
	public static Paint paint;
	public static LayoutInflater layoutInflater;
	
	private EnteringPanel enteringPanel;
	private LinearLayout offeredsLayout;
	private LinearLayout posTableLayout;
	private ScrollView scrollView;
	private final LinkedList<LinearLayout> freePosLayoutList = new LinkedList<LinearLayout>();

	private Toast alphabetNotSupportedToast;
//	private LayoutAnimationController lineInAnimation;
	private LinearLayout.LayoutParams charLP;
	private int displayWidth;
	private Button guessButton;

	private static final int DIALOG_ALPHABETS_ID = 0;
	private static final int DIALOG_SIZE_ID = 2;
	private static final int DIALOG_GIVEUP_ID = 3;
	private static final int DIALOG_CLEAR_MARKS_ID = 4;
	private static final int DIALOG_INTRO_ID = 5;
	
	private int wordSizeChosen;
	private int alphabetChosen;
	private boolean firstRun = false;
	private final Random rnd = new Random();

	private static Paint createPaint(Resources resources) {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(resources.getColor(R.drawable.font_color));
        paint.setDither(true);
        return paint;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.setData(Uri.parse("http://ya.ru"));
//		startActivity(intent);
        
		//init once per activity creation
		initActivity();
		
		run = null;
		try {
			run = restoreSavedRun();	//not null if restored
			re.run = run;
			reinitActivity();
			layoutViews();
		} 
		catch (Exception e) {
			Log.i(TAG, "restoreSavedState failed");
			firstRun = true;
			showDialog(DIALOG_INTRO_ID);	//invokes dialog chain of starting new game
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
		guessButton.setEnabled(!(Boolean)run.data.map.get(Run.ExtraData.DATA_GIVEN_UP));
		
		freePosLayoutList.clear();
		if (!(Boolean)run.data.map.get(Run.ExtraData.DATA_GIVEN_UP))
			enteringPanel = new EnteringPanel(this, this);
		ComparisonResultView.reset();
	}

	/**
	 *	Asks on Alphabet and WodLength in chain:
	 *	@see Main#initDialogs()
	 *	Ends up with finishStartingNewRun():
	 *	@see Main#finishStartingNewRun()
	 */
	private void startNewRun(boolean cancellable) {
		Log.v(TAG, "startNewRun");
		showDialog(DIALOG_ALPHABETS_ID);
    }

	private void finishStartingNewRun() {
		Alphabet alphabet = null;	
		String secret = null;
		switch (alphabetChosen) {
			case Alphabet.DIGITAL_ID: {
				alphabet = new Alphabet.Digital();
				secret = getRandomSybmols(alphabet, wordSizeChosen);
				break;
			}
			case Alphabet.LATIN_ID: {
				alphabet = new Alphabet.Latin();
				while (secret == null)	//make sure we don't crash in case loadWord caught exception somehow
					secret = loadWord(wordSizeChosen);
				break;
			}
		}

		try {
			run = re.startNewRun(alphabet, secret);
			Char[] secretLine = new Char[wordSizeChosen];
			for (int i = 0; i < wordSizeChosen; i++)
				secretLine[i] = Char.NULL;
			run.data.map.put(Run.ExtraData.DATA_SECRET_LINE, secretLine);
			run.data.map.put(Run.ExtraData.DATA_GIVEN_UP, false);
		} catch (BncException e) {	
			Log.e(TAG, e.toString());
		}
		
//		Log.d(TAG, run.secret.toString());
		
		//finish initialization, interrupted by dialogs chain
		reinitActivity();
		layoutViews();	
		firstRun = false;
	}
	
	private String getRandomSybmols(Alphabet alphabet, int size) {
		List<Character> secretList = alphabet.getSymbols();
		Collections.shuffle(secretList);
		StringBuffer sb = new StringBuffer();
		for (Character ch : secretList)
			sb.append(ch);
		return sb.substring(0, size);
	}
	
	private String loadWord(int wordLength) {
		Log.d(TAG, "loadWord: wordLength = " + wordLength);
		String word = null;
		String file = null;
		switch (wordLength) {
			case 4: 
				file = DICT_LATIN_4;		
				break;
			case 5: 
				file = DICT_LATIN_5;		
				break;
			case 6: 
				file = DICT_LATIN_6;		
				break;
		}
		
		InputStream stream = null;
		try {
			stream = getAssets().open(file);
			int fileSize = stream.available();
			int wordsCount = (int) (fileSize / (wordLength + 1));
			Log.d(TAG, "wordsCount = " + wordsCount);

			int wordNum = rnd.nextInt(wordsCount - 1);
			byte[] data = new byte[wordLength];
			stream.skip(wordNum * (wordLength + 1));
			stream.read(data);
			word = new String(data);
//			Log.d(TAG, "wordNum = " + wordNum + ", word = " + word);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} finally {
			if (stream != null)
				try { stream.close(); } catch (IOException e) {}
		}
		return word;
	}
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		Log.i(TAG, "onCreateDialog: id = " + id);
		Dialog dialog;
		switch (id) {	
			case DIALOG_ALPHABETS_ID:	//Dialogs chain - Alphabet then WordLength
				dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.alphabet_title)
				.setItems(R.array.alphabet_array, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						alphabetChosen = which;
						Log.i(TAG, "alphabet chosen: " + alphabetChosen);
						if (which == Alphabet.CYRILLIC_ID) {	
							if (alphabetNotSupportedToast == null)	//lazy init
								alphabetNotSupportedToast = Toast.makeText(Main.this, R.string.alphabet_not_supported_msg, 
										Toast.LENGTH_LONG);
							alphabetNotSupportedToast.show();
							return;
						}
						//ask size in chain
						showDialog(DIALOG_SIZE_ID);
					}
				})
				.create();
				Log.d(TAG, "onCreateDialog: DIALOG_ALPHABETS_ID created");
				break;
			case DIALOG_SIZE_ID:
				int itemsId = alphabetChosen == Alphabet.DIGITAL_ID ? R.array.num_word_length_array : R.array.word_length_array;
				dialog = new AlertDialog.Builder(this)
		        .setTitle(R.string.word_length_title)
		        .setItems(itemsId, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	wordSizeChosen = Alphabet.getMinSize(alphabetChosen) + which;
		            	Log.i(TAG, "word size chosen: " + wordSizeChosen);
		            	finishStartingNewRun();
		            }
		        })
		        .create();
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface arg0) {
						//clear size dialog instance - it should be initialized each time from the very beginning
						removeDialog(id);
						Log.d(TAG, "onDismiss: id = " + id);
					}
				});
				Log.d(TAG, "onCreateDialog: DIALOG_SIZE_ID created");
				break;
			case DIALOG_INTRO_ID:	//Dialogs chain - Intro then Alphabet then WordLength
				dialog = new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_info)
		        .setTitle(R.string.intro_title)
		        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						if (firstRun)	//very first run - start new game
							startNewRun(false);
					}
				})
		        .setMessage(R.string.intro_msg)
		        .create();
				Log.d(TAG, "onCreateDialog: DIALOG_INTRO_ID created");
				break;
			case DIALOG_GIVEUP_ID:
				dialog = new AlertDialog.Builder(this)
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
		        .setNegativeButton(android.R.string.cancel, null)
		        .setMessage(R.string.give_up_conf)
		        .create();
				Log.d(TAG, "onCreateDialog: DIALOG_GIVEUP_ID created");
				break;
			case DIALOG_CLEAR_MARKS_ID:
				dialog = new AlertDialog.Builder(this)
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
		        .setNegativeButton(android.R.string.cancel, null)
		        .setMessage(R.string.clear_marks_conf)
		        .create();
				Log.d(TAG, "onCreateDialog: DIALOG_CLEAR_MARKS_ID created");
				break;
			default: 
				dialog = null;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id == DIALOG_INTRO_ID || id == DIALOG_ALPHABETS_ID || id == DIALOG_SIZE_ID)
			dialog.setCancelable(!firstRun);
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
		new SecretWordPanel(secretLayout);
        
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
				resources.getDimensionPixelSize(R.dimen.scroll_right_padding) +
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
			FileOutputStream fos = openFileOutput(PERSISTENCE, MODE_PRIVATE);
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
			FileInputStream fis = openFileInput(PERSISTENCE);
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
			case R.id.AnswerLink:
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("http://translate.google.com?text=" + run.secret.asString()));
				startActivity(intent);
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
		boolean res = true;
		switch (item.getItemId()) {
			case R.id.MenuNewGame:
				startNewRun(true);
				break;
			case R.id.MenuClearMarks:
				showDialog(DIALOG_CLEAR_MARKS_ID);
				break;
			case R.id.MenuGiveUp:
				showDialog(DIALOG_GIVEUP_ID);
				break;
			case R.id.MenuIntro:
				showDialog(DIALOG_INTRO_ID);
				break;
			default:
				res = super.onOptionsItemSelected(item);
		}
		return res;
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
		compResView.setResult(wc.result);
		return compResView;
	}

	private void inflateAndShowAnswerDialog(int titleId, String message) {
		LinearLayout answerLayout = null;
		switch (Run.alphabet.getId()) {
			case Alphabet.DIGITAL_ID: {
				answerLayout = (LinearLayout) layoutInflater.inflate(R.layout.answerline_layout, null, false);
				inflateCharsLine(answerLayout, run.secret.chars, run.wordLength, null);
				break;
			}
			case Alphabet.CYRILLIC_ID:
			case Alphabet.LATIN_ID: {
				answerLayout = (LinearLayout) layoutInflater.inflate(R.layout.answer_layout, null, false);
				LinearLayout answerLine = (LinearLayout) answerLayout.findViewById(R.id.AnswerLine);
				inflateCharsLine(answerLine, run.secret.chars, run.wordLength, null);
				answerLayout.findViewById(R.id.AnswerLink).setOnClickListener(Main.this);
				break;
			}
		}

		Dialog dialog = new AlertDialog.Builder(Main.this)
            	.setIcon(android.R.drawable.ic_dialog_info)
            	.setTitle(titleId)
            	.setView(answerLayout)
            	.setMessage(message)
        		.setPositiveButton(android.R.string.ok, null)
        		.create();
		dialog.setOwnerActivity(this);
		dialog.show();
	}
	
	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, LayoutParams lp) {
		int viewId = R.layout.char_view;
		if (la.getId() == R.id.AnswerLine)
			viewId = R.layout.answer_char_view;
	
		for (int i = 0; i < length; i++) {
			CharView cv = (CharView) layoutInflater.inflate(viewId, la, false);
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

    		if (la.getId() == R.id.AnswerLine)
        		cv.changeStateOnClick = false;
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
			Char[] secretLine = (Char[]) run2.data.map.get(Run.ExtraData.DATA_SECRET_LINE);
			for (int i = 0; i < run2.wordLength; i++) {
				Char ch = secretLine[i];
				CharView cv = (CharView)layout.getChildAt(i);
				cv.setChar(ch, i == run2.posTable.getPresentPos(ch.ch));
				cv.changeStateOnClick = false;	//secret word should not react on clicks, just listen to changes produced by others
			}
			run2.posTable.addAllPosCharStateChangedListener(this);
		}

		public void onPosCharStateChanged(PosChar pch, ENCharState newState) {
			CharView cv = (CharView) layout.getChildAt(pch.pos);
			final Char[] secretLine = (Char[]) run2.data.map.get(Run.ExtraData.DATA_SECRET_LINE);
			if (newState == ENCharState.PRESENT) {
				try { 
					final Char ch = Char.valueOf(pch.ch, Run.alphabet);
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
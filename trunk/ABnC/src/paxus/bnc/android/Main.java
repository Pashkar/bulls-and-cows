 package paxus.bnc.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
import paxus.bnc.model.PositionTable.PositionLine;
import paxus.bnc.model.Run;
import paxus.bnc.model.Run.WordCompared;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

/**
 * @author paxus
 *
 */
public class Main extends Activity implements IPositionTableListener, OnClickListener, OnWordOfferedListener {
	
	private static final String TAG = "Main";

	private static final String PERSISTENCE = "persistence.dat";
	
	private static final String DICT_LATIN_4 = "latin_dict_4.txt";
	private static final String DICT_LATIN_5 = "latin_dict_5.txt";
	private static final String DICT_LATIN_6 = "latin_dict_6.txt";

	private static final String CYR_LATIN_4 = "cyr_dict_4.txt";
	private static final String CYR_LATIN_5 = "cyr_dict_5.txt";	
	private static final String CYR_LATIN_6 = "cyr_dict_6.txt";
	
	private static Paint paint;
	private static Paint paint_white;

	private final RunExecutor re = new RunExecutor();
	public static Run run;
	public static LayoutInflater layoutInflater;
	public static ContextWrapper context;
	
	private EnteringPanel enteringPanel;
	private LinearLayout offeredsLayout;
	private LinearLayout posTableLayout;
	private ScrollView scrollView;
	private final LinkedList<LinearLayout> freePosLayoutList = new LinkedList<LinearLayout>();

	private LinearLayout.LayoutParams charLP;
	private int displayWidth;
	private CharView guessButton;
	private CharView menuButton;

	private static final int DIALOG_ALPHABETS_ID = 0;
	private static final int DIALOG_SIZE_ID = 2;
	private static final int DIALOG_GIVEUP_ID = 3;
	private static final int DIALOG_CLEAR_MARKS_ID = 4;
	private static final int DIALOG_INTRO_ID = 5;
	
	private int wordSizeChosen = -1;
	private int alphabetChosen = -1;
	private boolean firstRun = false;
	private final Random rnd = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		
		//init once per activity creation
		initActivity();

		run = null;
		try {
			run = restoreSavedRun();	//not null if restored
			re.run = run;
			reinitPreLayout();
			layoutViews();
		} 
		catch (Exception e) {
			Log.i(TAG, "restoreSavedState failed");
			firstRun = true;
			showDialog(DIALOG_INTRO_ID);	//invokes dialog chain of starting new game
		}
    }

	private void initActivity() {
		Log.v(TAG, "initActivity");
		context = this;
		paint = createPaint(getResources().getColor(R.drawable.font_color_black));
		paint_white = createPaint(getResources().getColor(R.drawable.font_color_white));
		
    	layoutInflater = getLayoutInflater();
    	displayWidth = getWindowManager().getDefaultDisplay().getWidth();
	}

	private static Paint createPaint(int colorId) {
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16);
        paint.setTextAlign(Align.CENTER);
        paint.setColor(colorId);
        paint.setDither(true);
        return paint;
	}
    
	private void reinitPreLayout() {
		Log.v(TAG, "reinitPreLayout");
		setContentView(R.layout.main);
		
		offeredsLayout = (LinearLayout) findViewById(R.id.OfferedsLayout);
		posTableLayout = (LinearLayout) findViewById(R.id.PositioningLayout);
		scrollView = (ScrollView) findViewById(R.id.ScrollOffered);
		
		Boolean givenUp = (Boolean)run.data.map.get(Run.ExtraData.DATA_GIVEN_UP);
		guessButton = (CharView) findViewById(R.id.ShowAlphabetButton);
		guessButton.setEnabled(!givenUp);
		guessButton.setVisibility(givenUp ? View.INVISIBLE : View.VISIBLE);
		guessButton.setOnClickListener(this);

		menuButton = (CharView) findViewById(R.id.ShowMenuButton);
		menuButton.setOnClickListener(this);
		
		freePosLayoutList.clear();
		if (!givenUp)
			enteringPanel = new EnteringPanel(this, this);
		ComparisonResultView.reset();
	}

	/**
	 *	Asks on Alphabet and WordLength in chain:
	 *	@see Main#onCreateDialog()
	 *	Ends up with finishStartingNewRun():
	 *	@see Main#finishStartingNewRun()
	 */
	private void startNewRun(boolean cancellable) {
		Log.v(TAG, "startNewRun");
		showDialog(DIALOG_ALPHABETS_ID);
    }
	
	/**
	 * Initiates new run with different secret according to last used alphabet&word length. No dialog chain.
	 */
	private void startNewRunNoDialogs() {
		Log.v(TAG, "startNewRunNoDialogs");
		if (firstRun || run == null)
			startNewRun(false);

		alphabetChosen = Run.alphabet.getId();
		wordSizeChosen = run.wordLength;
		finishStartingNewRun();
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
				while (secret == null)	//make sure we don't crash in case loadRandomWord caught exception somehow
					secret = loadRandomWord(alphabetChosen, wordSizeChosen);
				break;
			}
			case Alphabet.CYRILLIC_ID: {
				alphabet = new Alphabet.Cyrrilic();
				while (secret == null)	//make sure we don't crash in case loadRandomWord caught exception somehow
					secret = loadRandomWord(alphabetChosen, wordSizeChosen);
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
		Log.i(TAG, "init new run: alphabetChosen=" + alphabetChosen + ", wordSizeChosen=" + wordSizeChosen);
		
		//finish initialization, interrupted by dialogs chain
		reinitPreLayout();
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
	
	private String loadRandomWord(int alphabet, int wordLength) {
		Log.d(TAG, "loadWord: alphabet = " + alphabet + ", wordLength = " + wordLength);
		String word = null;
		String file = null;
		switch (wordLength) {
			case 4: 
				file = alphabet == Alphabet.LATIN_ID ? DICT_LATIN_4 : CYR_LATIN_4;		
				break;
			case 5: 
				file = alphabet == Alphabet.LATIN_ID ? DICT_LATIN_5 : CYR_LATIN_5;		
				break;
			case 6: 
				file = alphabet == Alphabet.LATIN_ID ? DICT_LATIN_6 : CYR_LATIN_6;		
				break;
		}
		
		InputStream stream = null;
		try {
			stream = getAssets().open(file);
			final int fileSize = stream.available();
			final int lineLength = wordLength + 2;	//2 additional bytes are "\r\n"
			final int wordsCount = (fileSize / lineLength);
			Log.d(TAG, "fileSize = " + fileSize + ", wordLength = " + wordLength + ", wordsCount = " + wordsCount);

			int wordNum = rnd.nextInt(wordsCount);
			
			byte[] data = new byte[wordLength];
			stream.skip(wordNum * lineLength);	
			stream.read(data);
			Log.d(TAG, "wordNum = " + wordNum + ", data = " + Arrays.toString(data));
			if (alphabet == Alphabet.CYRILLIC_ID)
				word = new String(data, "Windows-1251");
			else
				word = new String(data);
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
							@Override
							public void onClick(DialogInterface dialog, int which) {
								alphabetChosen = which;
								Log.i(TAG, "alphabet chosen: " + alphabetChosen);
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
				            @Override
							public void onClick(DialogInterface dialog, int which) {
				            	wordSizeChosen = Alphabet.getMinSize(alphabetChosen) + which;
				            	Log.i(TAG, "word size chosen: " + wordSizeChosen);
				            	finishStartingNewRun();
				            }
				        })
				        .create();
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
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
							@Override
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
				            @Override
							public void onClick(DialogInterface dialog, int whichButton) {
				            	Log.i(TAG, "Give up");
				            	inflateAndShowAnswerDialog(R.string.secret_title, getResources().getString(R.string.give_up_msg));
				            	guessButton.setEnabled(false);
				            	guessButton.setVisibility(View.INVISIBLE);
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
				            @Override
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
		Log.v(TAG, "layoutViews");
		Run run2 = run;
		PositionTable posTable = run2.posTable;
		
		calcOfferedCharLayout();
		
        //inflate secret word layout
        LinearLayout secretLayout = (LinearLayout) findViewById(R.id.SecretLayout);
		inflateCharsLine(secretLayout, null, run2.wordLength, charLP);
		new SecretWordPanel(secretLayout);
        
        //inflate all rows for PositionTable, store prepared lines in list for further usage
		//also inflate labels
        LinkedList<LinearLayout> freePosLayoutList2 = freePosLayoutList;
        for (int i = 0; i < run2.wordLength; i++) {
        	freePosLayoutList2.add(inflatePosLine(R.layout.posline_layout));
        }
        posTable.addPosTableListener(this);
        
        //inflate one more row - a dummy one, 1px height - to make sure entire pos table have a content of valid width
        LinearLayout dummyPosLine = inflatePosLine(R.layout.posline_dummy_layout);
        dummyPosLine.setVisibility(View.INVISIBLE);
		posTableLayout.addView(dummyPosLine);
        
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

	@Override
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

	@Override
	public void onClick(View v) {
		Log.v(TAG, "onClick: " + v + ", " + v.getId());
		switch (v.getId()) {
			case R.id.ShowAlphabetButton:
				enteringPanel.show();
				break;
			case R.id.AnswerLink:
				Log.d(TAG, "onClick: AnswerLink, secret = " + run.secret.asString() + ", Uri = " + Uri.encode(run.secret.asString()));
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("http://translate.google.com?text=" + Uri.encode(run.secret.asString())));
				startActivity(intent);
				break;
			case R.id.ShowMenuButton:
				Log.v(TAG, "R.id.ShowMenuButton");
				openOptionsMenu();
				break;
		}
	}
	
	@Override
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
			case R.id.MenuNewGameNoDialogs:
				startNewRunNoDialogs();
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
	
	public static Paint getPaint(int colorResId) {
		if (colorResId == R.drawable.font_color_white)
			return paint_white;
		return paint;		//default
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
        		.setPositiveButton(R.string.new_game_title, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startNewRunNoDialogs();
						}
				})
        		.setNegativeButton(R.string.close_caption, null)
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
        	la.addView(cv);
        	cv.setVisibility(View.VISIBLE);
        }
	}
	
	//inflate PosCharViews with PosChar.NULL values
	private LinearLayout inflatePosLine(int lineLayoutResId) {
		LayoutInflater layoutInflater2 = layoutInflater;
		int wordLength = run.wordLength;
		LinearLayout line = (LinearLayout) layoutInflater2.inflate(lineLayoutResId, posTableLayout, false);
		
		for (int i = 0; i < wordLength; i++) {
			PosCharView pcw = (PosCharView) layoutInflater2.inflate(R.layout.poschar_view, line, false);
			pcw.setLayoutParams(charLP);
        	line.addView(pcw);
        }
		return line;
	}

	private class SecretWordPanel implements IPosCharStateChangedListener {
		
		public LinearLayout layout;
		private final Run run2;

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

		@Override
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
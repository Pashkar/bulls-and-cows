package paxus.bnc.android;

import paxus.bnc.android.view.CharView;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.Run;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public final class EnteringPanel implements OnClickListener, android.content.DialogInterface.OnClickListener {

	private static final String TAG = "EnteringPanel";

	private StringBuffer enteringWord;

	private LinearLayout enteringWordLayout;

	private Toast tooLongWordToast;

	private Toast duplicatedCharToast;

	private Run run;

	private View panelView;

	private AlertDialog panelDialog;	//implementation based on Dialog

	private final OnWordOfferedListener callback;

	private LayoutParams digitalAlphaberCharLP;

	private final Context context;

	public EnteringPanel(Context context, OnWordOfferedListener callback) {
		this.context = context;
		Log.v(TAG, "<init>");
		this.callback = callback;
		this.run = Main.run;
		Run run2 = run;
		tooLongWordToast = Toast.makeText(context, R.string.word_too_long_msg, Toast.LENGTH_SHORT);
		tooLongWordToast.setGravity(Gravity.TOP, 0, 50);
		duplicatedCharToast = Toast.makeText(context, R.string.diplicated_msg, Toast.LENGTH_SHORT);
		duplicatedCharToast.setGravity(Gravity.TOP, 0, 50);
		
		int alphabetLayoutId = -1;
		if (Alphabet.DIGITAL.equals(run2.alphabet.getName()))
			alphabetLayoutId = R.layout.digital_alphabet;
		//TODO other alphabets
		
		panelView = Main.layoutInflater.inflate(alphabetLayoutId, null);

		enteringWordLayout = (LinearLayout) panelView.findViewById(R.id.EnteringLayout);
		inflateCharsLine(enteringWordLayout, null, 0, run2.wordLength, R.layout.char_view, -1);
		inflateAlphabetLines(alphabetLayoutId);
		enteringWord = new StringBuffer(run2.wordLength);
		
        panelDialog = new AlertDialog.Builder(context)
		.setPositiveButton(android.R.string.ok, this)
		.setNegativeButton(R.string.clear, this)
		.setView(panelView)
		.create();
        panelDialog.setCanceledOnTouchOutside(true);
	}

	private void inflateAlphabetLines(int alphabetLayoutId) {
		Char[] chars = run.alphabet.getAllChars().toArray(new Char[10]);
		switch (alphabetLayoutId) {
			case R.layout.digital_alphabet:	//2 lines
				LinearLayout line1 = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout_line1);
				inflateCharsLine(line1, chars, 0, 5, R.layout.digital_alphabet_char_view, R.id.DigitalAlphabetCharView);
				LinearLayout line2 = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout_line2);
				inflateCharsLine(line2, chars, 5, 10, R.layout.digital_alphabet_char_view, R.id.DigitalAlphabetCharView);
			break;
		}
	}

	//Char click
	public void onClick(View v) {
		if (enteringWord.length() >= run.wordLength) {
			tooLongWordToast.show();
			return;
		}
		
		CharView cv = (CharView) v;
		Character ch = cv.getChar().ch;
		//duplicates are not allowed
		if (enteringWord.indexOf("" + ch) != -1) {
			duplicatedCharToast.show();
			return;
		}
			
		enteringWord.append(ch);
		int curPos = enteringWord.length() - 1;
		CharView ecv = (CharView)enteringWordLayout.getChildAt(curPos);
		//for newly added char PosTable may have already set position and no updates will be sent - force posMatched
		ecv.setChar(cv.getChar(), run.posTable.getPresentPos(ch) == curPos);		
	}


	//Dialog buttons
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:	//"ok"
			String word = enteringWord.toString();
			if (word.length() == run.wordLength) {
				callback.onWordOffered(word);
				clearWord();
			}
			break;
		case DialogInterface.BUTTON_NEGATIVE:	//"clear"
			clearWord();
			break;
		}
	}
	
	private void clearWord() {
		for (int i = 0; i < run.wordLength; i++)
			((CharView)enteringWordLayout.getChildAt(i)).resetChar();
		enteringWord = new StringBuffer(run.wordLength);
	}
	
	private void inflateCharsLine(LinearLayout la, Char[] chars, int from, int to, int layoutId, int viewId) {
		for (int i = from; i < to; i++) {
        	CharView cv = (CharView) Main.layoutInflater.inflate(layoutId, la, false);
        	cv.paint = Main.paint;
        	if (chars != null)
        		cv.setChar(chars[i]);
        	if (viewId != -1)
        		cv.setId(viewId);
        	if (viewId == R.id.DigitalAlphabetCharView) {
        		if (digitalAlphaberCharLP == null) {
					Resources resources = context.getResources();
					//lazy init
					digitalAlphaberCharLP = new LayoutParams(resources.getDimensionPixelSize(R.dimen.entering_char_width), 
							resources.getDimensionPixelSize(R.dimen.entering_char_width));
				}
        		cv.setLayoutParams(digitalAlphaberCharLP);
        		cv.setOnClickListener(this);
        		cv.changeStateOnClick = false;
        	}
        	if (la.getId() == R.id.EnteringLayout) {
				cv.setViewPos(i);
				cv.changeStateOnClick = false;
				run.posTable.addAllPosCharStateChangedListener(cv);
        	}
        	la.addView(cv);
        }
	}

	public void show() {
		Log.v(TAG, "show");
		panelDialog.show();
	}
}

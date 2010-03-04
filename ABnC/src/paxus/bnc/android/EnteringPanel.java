package paxus.bnc.android;

import paxus.bnc.android.view.CharView;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.Run;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

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

	public EnteringPanel(Context context, OnWordOfferedListener callback) {
		Log.v(TAG, "<init>");
		this.callback = callback;
		this.run = Main.run;
		Run run2 = run;
		int offset = context.getResources().getDimensionPixelOffset(R.dimen.toast_top_margin);
		tooLongWordToast = Toast.makeText(context, R.string.word_too_long_msg, Toast.LENGTH_SHORT);
		tooLongWordToast.setGravity(Gravity.TOP, 0, offset);
		duplicatedCharToast = Toast.makeText(context, R.string.diplicated_msg, Toast.LENGTH_SHORT);
		duplicatedCharToast.setGravity(Gravity.TOP, 0, offset);
		
		int alphabetLayoutId = -1;
		int alphabetId = Run.alphabet.getId();
		switch (alphabetId) {
			case Alphabet.DIGITAL_ID:
				alphabetLayoutId = R.layout.entering_digital_layout;
				break;
			case Alphabet.LATIN_ID:
				alphabetLayoutId = R.layout.entering_latin_layout;
				break;
		}
		
		panelView = Main.layoutInflater.inflate(alphabetLayoutId, null);

		enteringWordLayout = (LinearLayout) panelView.findViewById(R.id.EnteringLayout);
		inflateCharsLine(enteringWordLayout, null, 0, run2.wordLength, R.layout.entering_char_view);
		inflateAlphabetLines(alphabetId);
		enteringWord = new StringBuffer(run2.wordLength);
		
        panelDialog = new AlertDialog.Builder(context)
		.setPositiveButton(android.R.string.ok, this)
		.setNegativeButton(R.string.clear_title, this)
		.setView(panelView)
		.create();
        panelDialog.setCanceledOnTouchOutside(true);
	}

	private void inflateAlphabetLines(int alphabetId) {
		Char[] chars = Run.alphabet.getAllCharsSorted().toArray(new Char[10]);
		switch (alphabetId) {
			case Alphabet.DIGITAL_ID:	{	//2 x 5 
				LinearLayout line;
				line = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout_line1);
				inflateCharsLine(line, chars, 0, 5, R.layout.alphabet_digital_char_view);
				line = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout_line2);
				inflateCharsLine(line, chars, 5, 5, R.layout.alphabet_digital_char_view);
				break;
			}
			case Alphabet.LATIN_ID:	{	//3 x 6 & 1 x 7
				LinearLayout line;
				line = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout_line1);
				inflateCharsLine(line, chars, 0, 6, R.layout.alphabet_6_char_view, R.layout.alphabet_6_last_char_view);
				line = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout_line2);
				inflateCharsLine(line, chars, 6, 6, R.layout.alphabet_6_char_view, R.layout.alphabet_6_last_char_view);
				line = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout_line3);
				inflateCharsLine(line, chars, 12, 6, R.layout.alphabet_6_char_view, R.layout.alphabet_6_last_char_view);
				line = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout_line4);
				inflateCharsLine(line, chars, 18, 7, R.layout.alphabet_7_char_view);
				break;
			}
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
	
	private void inflateCharsLine(LinearLayout la, Char[] chars, int from, int length, int layoutId) {
		inflateCharsLine(la, chars, from, length, layoutId, -1);
	}
	
	private void inflateCharsLine(LinearLayout la, Char[] chars, int from, int length, int layoutId, int lastLayoutId) {
		int to = from + length;
		for (int i = from; i < to; i++) {
			int lId = layoutId;
			if (i == to - 1 && lastLayoutId != -1)
				lId = lastLayoutId;
        	CharView cv = (CharView) Main.layoutInflater.inflate(lId, la, false);
        	cv.paint = Main.paint;
        	cv.changeStateOnClick = false;
        	if (chars != null)
        		cv.setChar(chars[i]);
    		if (la.getId() == R.id.EnteringLayout) {
				cv.setViewPos(i);
				run.posTable.addAllPosCharStateChangedListener(cv);
        	} else {	//alphabet line layout
        		cv.setOnClickListener(this);
        	}
        	la.addView(cv);
        }
	}

	public void show() {
		Log.v(TAG, "show");
		panelDialog.show();
	}
}

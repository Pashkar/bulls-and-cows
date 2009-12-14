package paxus.bnc.android;

import paxus.bnc.android.view.CharView;
import paxus.bnc.model.Alphabet;
import paxus.bnc.model.Char;
import paxus.bnc.model.Run;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public final class EnteringPanel implements OnClickListener {

	private StringBuffer enteringWord;

	private LinearLayout enteringWordLayout;

	private LinearLayout alphabetLayout;

	private Toast tooLongWordToast;

	private Toast duplicatedCharToast;

	private Run run;

	private View panelView;

	private AlertDialog panelDialog;	//implementation based on Dialog

	private final OnWordOfferedListener callback;

	public EnteringPanel(Context context, OnWordOfferedListener callback) {
		Log.v("EnteringPanel", "<init>");
		this.callback = callback;
		this.run = Main.run;
		Run run2 = run;
		tooLongWordToast = Toast.makeText(context, R.string.word_too_long_msg, Toast.LENGTH_SHORT);
		duplicatedCharToast = Toast.makeText(context, R.string.diplicated_msg, Toast.LENGTH_SHORT);
		
		int alphabetLayoutId = -1;
		if (Alphabet.DIGITAL.equals(run2.alphabet.getName()))
			alphabetLayoutId = R.layout.digital_alphabet_for_dialog;
		//TODO other alphabets
		
		Log.i("EnteringPanel", "alphabetLayoutId = " + alphabetLayoutId);
		panelView = Main.layoutInflater.inflate(alphabetLayoutId, null);

		enteringWordLayout = (LinearLayout) panelView.findViewById(R.id.EnteringLayout);
		inflateCharsLine(enteringWordLayout, null, run2.wordLength , -1);
		alphabetLayout = (LinearLayout) panelView.findViewById(R.id.AlphabetLayout);
		inflateCharsLine(alphabetLayout, run2.alphabet.getAllChars().toArray(new Char[10]), 10, R.id.AlphabetCharView);
		
        panelDialog = new AlertDialog.Builder(context)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				EnteringPanel.this.onOk();
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		})
		.setView(panelView)
		.create();
	}

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

	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, int viewId) {
		for (int i = 0; i < length; i++) {
        	CharView cv = (CharView) Main.layoutInflater.inflate(R.layout.char_view, la, false);
        	cv.paint = Main.paint;
        	if (chars != null)
        		cv.setChar(chars[i]);
        	if (viewId != -1)
        		cv.setId(viewId);
        	if (la.getId() == R.id.AlphabetLayout)
        		cv.setOnClickListener(this);
        	if (la.getId() == R.id.EnteringLayout) {
				cv.setViewPos(i);
				cv.changeStateOnClick = false;
        	}
        	la.addView(cv);
        }
	}
	
	private void onOk() {
		callback.onWordOffered(enteringWord.toString());
	}

	public void show() {
		Log.v("EnteringPanel", "show");
		for (int i = 0; i < run.wordLength; i++)
			((CharView)enteringWordLayout.getChildAt(i)).resetChar();
		enteringWord = new StringBuffer(run.wordLength);
		panelDialog.show();
	}
}

package paxus.bnc.android;

import paxus.bnc.android.view.CharView;
import paxus.bnc.model.Char;
import paxus.bnc.model.Run;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DigitalAlphabetActivity extends Activity implements OnClickListener{

	private Paint paint;
	private LinearLayout enteringWordLayout;
	private Run run; 

	private StringBuffer enteringWord;
	private View okButton;
	private View delButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        setContentView(R.layout.digital_alphabet);
        
        initActivity();
	}
	
	private void initActivity() {
		Log.v("DigitalAlphabetActivity", "initActivity");
		run = Main.run;
		
		paint = Main.createPaint(getResources());
		enteringWordLayout = (LinearLayout)findViewById(R.id.EnteringLayout);
		enteringWord = new StringBuffer();
		
		delButton = findViewById(R.id.DelButton);
		delButton.setOnClickListener(this);
		okButton = findViewById(R.id.OkButton);
		okButton.setOnClickListener(this);
		findViewById(R.id.CancelButton).setOnClickListener(this);
		
		initAllViews();
	}
	
	private void initAllViews() {
		//inflate entering word layout
		inflateCharsLine(enteringWordLayout, 
        		null, run.wordLength , -1);
		
        inflateCharsLine((LinearLayout) findViewById(R.id.DigitalAlphabetLayout), 
        		run.alphabet.getAllChars().toArray(new Char[10]), 10, R.id.AlphabetCharView);
	}
	
	private void inflateCharsLine(LinearLayout la, Char[] chars, int length, int viewId) {
		for (int i = 0; i < length; i++) {
        	CharView cv = (CharView) getLayoutInflater().inflate(R.layout.char_view, la, false);
        	cv.paint = paint;
        	if (chars != null)
        		cv.setChar(chars[i]);
        	if (viewId != -1)
        		cv.setId(viewId);
        	if (viewId == R.id.AlphabetCharView)
        		cv.setOnClickListener(this);	//enter new word by clicks
			if (la.getId() == R.id.EnteringLayout) {
				cv.setViewPos(i); 				// to mark "bull" in these words
				run.posTable.addAllPosCharStateChangedListener(cv);
			}
        	la.addView(cv);
        }
	}


	public void onClick(View v) {
		Log.v("DigitalAlphabetActivity", "onClick");
		int curPos;
		CharView ecv;

		switch (v.getId()) {
			case R.id.AlphabetCharView:
					
				if (enteringWord.length() >= run.wordLength) {
					Toast.makeText(this, R.string.word_too_long_msg, Toast.LENGTH_SHORT).show();
					return;
				}
				
				CharView cv = (CharView) v;
				Character ch = cv.getChar().ch;
				//duplicates are not allowed
				if (enteringWord.indexOf("" + ch) != -1) {
					Toast.makeText(this, R.string.diplicated_msg, Toast.LENGTH_SHORT).show();
					return;
				}
					
				enteringWord.append(ch);
				curPos = enteringWord.length() - 1;
				ecv = (CharView)enteringWordLayout.getChildAt(curPos);
				//for newly added char PosTable may have already set position and no updates will be sent - force posMatched
				ecv.setChar(cv.getChar(), run.posTable.getPresentPos(ch) == curPos);
				
				okButton.setEnabled(enteringWord.length() >= run.wordLength);
				delButton.setEnabled(enteringWord.length() > 0);
				break;
			case R.id.DelButton:
				curPos = enteringWord.length() - 1;
				if (curPos >= 0) {
					ecv = (CharView)enteringWordLayout.getChildAt(curPos);
					ecv.resetChar();
					enteringWord.deleteCharAt(curPos);
				}
				delButton.setEnabled(enteringWord.length() > 0);
				break;
			case R.id.CancelButton:
				setResult(RESULT_CANCELED, null);
	            finish();
				break;
			case R.id.OkButton:
				setResult(RESULT_OK, (new Intent()).setAction(enteringWord.toString()));
	            finish();
			break;
		}
	}
	
}

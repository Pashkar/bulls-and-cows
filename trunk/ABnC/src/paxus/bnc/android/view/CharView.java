package paxus.bnc.android.view;

import paxus.bnc.BncException;
import paxus.bnc.android.R;
import paxus.bnc.controller.ICharStateChangedListener;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class CharView extends View implements OnClickListener, ICharStateChangedListener, 
		IPosCharStateChangedListener {

	private static final String TAG = "CharView";
	public static final int WIDTH = 20;
	public static final int HEIGHT = 20;
	static Animation anim;
	
	public Paint paint;

	private Char ch = Char.NULL;
	
	public boolean changeStateOnClick = true; 
	
	/**
	 *	For "bull" when position of CharView pointed. Stores for the CharView instance position of it in word.
	 *	Compared to position in PosTable, marked by user 
	 */
	private int viewPos = -1;	
	
	private boolean posMatched = false;
	private int xOffset = -1;
	private int yOffset = -1;
	private OnClickListener clickListener;
	
	public CharView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	public CharView(Context context) {
		super(context);
		initView(context);
	}

	public void initView(Context context) {
		super.setOnClickListener(this);
        if (anim == null)
        	anim = AnimationUtils.loadAnimation(context, R.anim.char_fade_in_anim);
	}

	public void setChar(Char ch, boolean posMatched) {
		this.posMatched = posMatched;
		setChar(ch);
	}
	
	public void setChar(Char ch) {
		this.ch = ch;
		ch.addStateChangedListener(this);
//		invalidate();
		setBackground();
		startAnimation(anim);
	}
	
	public Char getChar() {
		return ch;
	}
	
	public void resetChar() {
		ch.removeStateChangedListener(this);
		ch = Char.NULL;
		posMatched = false;
		clickListener = null;
//		invalidate();
		setBackground();
		startAnimation(anim);
	}
	
	/**
	 *	For "bull" when position of CharView pointed. Stores for the CharView instance position of it in word.
	 *	Compared to position in PosTable, marked by user 
	 */
	public void setViewPos(int pos) {
		this.viewPos = pos;
//		invalidate();
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }
     
    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            return specSize;
        } else {
        	int desiredWidth = WIDTH + getPaddingLeft() + getPaddingRight();
        	if (specMode == MeasureSpec.AT_MOST) {
        		return desiredWidth < specSize ? desiredWidth : specSize;
        	} else {
        		return desiredWidth;
        	}
        }
    }
    
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            return specSize;
        } else {
        	int desiredHeight = HEIGHT + getPaddingTop() + getPaddingBottom();
        	if (specMode == MeasureSpec.AT_MOST) {
        		return desiredHeight < specSize ? desiredHeight : specSize;
        	} else {
        		return desiredHeight;
        	}
        }
    }
    
    protected void onDraw(Canvas canvas) {
        setBackground();
        if (ch != null && paint != null) {
        	if (xOffset == -1)
        		xOffset = getPaddingLeft() + getWidth() / 2;
        	if (yOffset == -1)
        		yOffset = getHeight() / 2 + (int)paint.getTextSize() / 2;
			canvas.drawText("" + ch.ch, xOffset, yOffset, paint);
		}
    }

	//TODO not Background (probably it's stretched), just draw. Use Prescaled
	//TODO use 9 points pictures
	//TODO ImageView from xml and then "image.setImageResource(R.drawable.android);" or just from xml
	private void setBackground() {
		if (ch == null)
			return;
		switch (ch.getState()) {
		case NONE:
			setBackgroundResource(R.drawable.noth);
			break;
		case ABSENT:
			setBackgroundResource(R.drawable.wrong);
			break;
		case PRESENT:
			setBackgroundResource(posMatched ? R.drawable.bull : R.drawable.cow);
			break;
		}
	}
	
	public void onClick(View v) {
		try {
			Log.d(TAG, v + ".onClick()");
			if (clickListener != null)
				clickListener.onClick(v);
			
			startAnimation(anim);
			if (changeStateOnClick) 
				ch.moveState();		//if state really changes - onStateChanged will be notified
		} catch (BncException e) {};
	}
	
	//Once we start receiving events on one touch action, we receive all the rest until touch released. 
	//No way for another view to start receiving events even if actions is dragged far from this one.
	//Decided to handle touch events on higher level of view hierarchy and pass down to view under a finger.
/*	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.v(TAG, this + ": onTouchEvent, event = " + event + ", witdh = " + getWidth());
		boolean res = event.getX() <= 30;
		Log.v(TAG, "return " + res);
		return res;
	}*/

	public void onCharStateChanged(Character ch, ENCharState newState) {
//		invalidate();
		startAnimation(anim);
	}

	public void onPosCharStateChanged(PosChar ch, ENCharState newState) {
		boolean oldMatched = posMatched;
		if (viewPos != -1 && ch.ch == this.ch.ch && ch.pos == viewPos)
			posMatched = (newState == ENCharState.PRESENT);
		if (oldMatched != posMatched)
//			invalidate();
			startAnimation(anim);
	}
	
	@Override
	public String toString() {
		return ch + "";
	}

	/** 
	 * CharView is always OnClickListener for itself. External objects may add additional listeners and 
	 * event would be replicated (android.view.View supports only one listener and for CharView it is occupied by itself).
	 * Currently only one external listener supported - to be widened when needed.
	 */
	@Override
	public final void setOnClickListener(OnClickListener l) {
//		super.setOnClickListener(l);
		clickListener = l;
	}
}

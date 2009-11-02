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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class CharView extends View implements OnClickListener, ICharStateChangedListener, 
		IPosCharStateChangedListener {

	public static final int WIDTH = 20;
	public static final int HEIGHT = 20;
	static Animation anim;
	
	public Paint paint;

	private Char ch = Char.NULL;
	
	/**
	 *	For "bull" when position of CharView pointed. Stores for the CharView instance position of it in word.
	 *	Compared to position in PosTable, marked by user 
	 */
	private int viewPos = -1;	
	
	private boolean posMatched = false;
	

	public CharView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	public CharView(Context context) {
		super(context);
		initView(context);
	}

	public void initView(Context context) {
		setOnClickListener(this);
        if (anim == null)
        	anim = AnimationUtils.loadAnimation(context, R.anim.char_fade_in_anim);
	}
	
	public void setChar(Char ch) {
		this.ch = ch;
		ch.addStateChangedListener(this);
//		invalidate();
		startAnimation(anim);
	}

	public void setChar(Char ch, boolean posMatched) {
		this.ch = ch;
		this.posMatched = posMatched;
		ch.addStateChangedListener(this);
//		invalidate();
		startAnimation(anim);
	}
	
	public Char getChar() {
		return ch;
	}
	
	public void resetChar() {
		ch.removeStateChangedListener(this);
		ch = Char.NULL;
		posMatched = false;
//		invalidate();
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
        super.onDraw(canvas);
        drawBackground(canvas);
        canvas.drawText("" + ch.ch, getPaddingLeft() + getWidth() / 2, getPaddingTop() + getHeight(), paint);
    }

	public void onClick(View v) {
		try {
			ch.moveState();		//if state really changes - onStateChanged will be notified
		} catch (BncException e) {};
	}
	
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

	//TODO not Background (probably it's stretched), just draw. Use Prescaled
	//TODO use 9 points pictures
	//TODO ImageView from xml and then "image.setImageResource(R.drawable.android);" or just from xml
	private void drawBackground(Canvas canvas) {
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
	
	@Override
	public String toString() {
		return ch + "";
	}
}

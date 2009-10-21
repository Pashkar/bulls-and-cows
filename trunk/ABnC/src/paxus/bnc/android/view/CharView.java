package paxus.bnc.android.view;

import paxus.bnc.BncException;
import paxus.bnc.controller.ICharStateChangedListener;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class CharView extends View implements OnClickListener, ICharStateChangedListener, 
		IPosCharStateChangedListener {

	private static final int WIDTH = 14;
	private static final int HEIGHT = 14;
	
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
		initView();
	}
	
	public CharView(Context context) {
		super(context);
		initView();
	}

	public void initView() {
		setOnClickListener(this);
	}
	
	public void setChar(Char ch) {
		this.ch = ch;
		ch.addStateChangedListener(this);
		invalidate();
	}

	public void setChar(Char ch, boolean posMatched) {
		this.ch = ch;
		this.posMatched = posMatched;
		ch.addStateChangedListener(this);
		invalidate();
	}
	
	public Char getChar() {
		return ch;
	}
	
	public void resetChar() {
		ch.removeStateChangedListener(this);
		ch = Char.NULL;
		posMatched = false;
//		invalidate();
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
        return WIDTH + getPaddingLeft() + getPaddingRight();
    }

    private int measureHeight(int measureSpec) {
        return HEIGHT + getPaddingTop() + getPaddingBottom();
    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        canvas.drawText("" + ch.ch, getPaddingLeft() + WIDTH / 2, getPaddingTop() + HEIGHT, paint);
    }

	public void onClick(View v) {
		try {
			ch.moveState();		//if state really changes - onStateChanged will be notified
		} catch (BncException e) {};
	}
	
	public void onCharStateChanged(Character ch, ENCharState newState) {
		invalidate();
	}

	public void onPosCharStateChanged(PosChar ch, ENCharState newState) {
		boolean oldMatched = posMatched;
		if (viewPos != -1 && ch.ch == this.ch.ch && ch.pos == viewPos)
			posMatched = (newState == ENCharState.PRESENT);
		if (oldMatched != posMatched)
//			postInvalidate();
			invalidate();
	}

	//TODO not Background (probably it's stretched), just draw. Use Prescaled
	private void drawBackground(Canvas canvas) {
		switch (ch.getState()) {
		case NONE:
			setBackgroundColor(Color.GREEN);
//			setBackgroundResource(R.drawable.noth);
			break;
		case ABSENT:
			setBackgroundColor(Color.RED);
//			setBackgroundResource(R.drawable.wrong);
			break;
		case PRESENT:
			setBackgroundColor(posMatched ? Color.DKGRAY : Color.GRAY);
//			setBackgroundResource(posMatched ? R.drawable.bull : R.drawable.cow);
			break;
		}
	}
	
	@Override
	public String toString() {
		return ch + "";
	}
}

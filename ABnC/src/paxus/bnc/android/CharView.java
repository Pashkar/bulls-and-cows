package paxus.bnc.android;

import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.OnStateChangedListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class CharView extends View implements OnClickListener, OnStateChangedListener {

	private static final int WIDTH = 14;
	private static final int HEIGHT = 14;
	
	Paint paint;

	private Char ch = Char.NO_ALPHA; 

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
        canvas.drawText(ch.asString, getPaddingLeft() + WIDTH / 2, getPaddingTop() + HEIGHT, paint);
    }

	public void onClick(View v) {
		ch.moveState(ICharStateSequencer.FORWARD);	//if state really changes - onStateChanged will be notified
	}
	
	public void onStateChanged(Character ch, ENCharState newState) {
		changeBackground(newState);
		invalidate();
	}

	private void changeBackground(ENCharState state) {
		switch (state) {
		case NONE:
			setBackgroundResource(R.drawable.noth);
			break;
		case ABSENT:
			setBackgroundResource(R.drawable.wrong);
			break;
		case PRESENT:
			setBackgroundResource(R.drawable.cow);
			break;
		}
	}

	@Override
	public String toString() {
		return ch + "";
	}
}

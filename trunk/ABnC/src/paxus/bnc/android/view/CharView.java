package paxus.bnc.android.view;

import paxus.bnc.BncException;
import paxus.bnc.android.R;
import paxus.bnc.controller.ICharStateChangedListener;
import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class CharView extends View implements OnClickListener, ICharStateChangedListener {

	private static final int WIDTH = 14;
	private static final int HEIGHT = 14;
	
	public Paint paint;

	private Char ch = Char.NULL; 

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
	
	public Char getCh() {
		return ch;
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
//		setBackground(newState);
		invalidate();
	}

	//TODO not Background (probably it's stretched), just draw. Use Prescaled
	private void drawBackground(Canvas canvas) {
		switch (ch.getState()) {
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

package paxus.bnc.android.view;

import paxus.bnc.android.R;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class PosCharView extends View implements OnClickListener, IPosCharStateChangedListener {
	private static final int WIDTH = 14;
	private static final int HEIGHT = 14;
	
	public Paint paint;

	private PosChar pch = PosChar.NULL; 

	public PosCharView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public PosCharView(Context context) {
		super(context);
		initView();
	}

	public void initView() {
		setOnClickListener(this);
	}
	
	public void setPosChar(PosChar pch) {
		this.pch = pch;
		pch.addPosStateChangedListener(this);
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
        canvas.drawText("[" + pch.ch + "]", getPaddingLeft() + WIDTH / 2, getPaddingTop() + HEIGHT, paint);
    }

	public void onClick(View v) {
		pch.movePosState();	//if state really changes - onStateChanged will be notified
	}
	
	public void onPosCharStateChanged(PosChar pch, ENCharState newState) {
		if (this.pch != pch)
			return;
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
		return pch + "";
	}
}

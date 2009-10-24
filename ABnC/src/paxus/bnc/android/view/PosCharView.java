package paxus.bnc.android.view;

import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
		setOnClickListener(this);
	}
	
	public PosCharView(Context context) {
		super(context);
		setOnClickListener(this);
	}

	public void setPosChar(PosChar pch) {
		this.pch = pch;
		pch.addPosStateChangedListener(this);
	}
	
	public void clearPosChar() {
		pch.removePosStateChangedListener(this);
		pch = PosChar.NULL;
		invalidate();
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
        canvas.drawText("[" + pch.ch + "]", getPaddingLeft() + WIDTH / 2, getPaddingTop() + HEIGHT, paint);
    }

	public void onClick(View v) {
		pch.movePosState();	//if state really changes - onStateChanged will be notified
	}
	
	public void onPosCharStateChanged(PosChar pch, ENCharState newState) {
		if (this.pch != pch)
			return;
		invalidate();
	}

	private void drawBackground(Canvas canvas) {
		switch (pch.state) {
		case NONE:
			setBackgroundColor(Color.GREEN);
//			setBackgroundResource(R.drawable.noth);
			break;
		case ABSENT:
			setBackgroundColor(Color.RED);
//			setBackgroundResource(R.drawable.wrong);
			break;
		case PRESENT:
			setBackgroundColor(Color.GRAY);
//			setBackgroundResource(R.drawable.cow);
			break;
		}
	}

	@Override
	public String toString() {
		return pch + "";
	}
}

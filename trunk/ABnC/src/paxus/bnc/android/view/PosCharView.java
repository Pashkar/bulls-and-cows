package paxus.bnc.android.view;

import static paxus.bnc.android.view.CharView.anim;
import paxus.bnc.android.R;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class PosCharView extends View implements OnClickListener, IPosCharStateChangedListener {
	
	private static final String TAG = "PosCharView";

	public Paint paint;

	private PosChar pch = PosChar.NULL;

	private boolean hideOnDraw = false;

	private int xOffset = -1;

	private int yOffset = -1;
	
	public PosCharView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PosCharView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		setOnClickListener(this);
	}
	
	public void setPosChar(PosChar pch) {
		this.pch = pch;
		pch.addPosStateChangedListener(this);
		hideOnDraw = false;
	}
	
	public void resetPosChar() {
		pch.removePosStateChangedListener(this);
		pch = PosChar.NULL;
		hideOnDraw = false;
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
        	int desiredWidth = getLayoutParams().width + getPaddingLeft() + getPaddingRight();
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
        	int desiredHeight = getLayoutParams().height + getPaddingTop() + getPaddingBottom();
        	if (specMode == MeasureSpec.AT_MOST) {
        		return desiredHeight < specSize ? desiredHeight : specSize;
        	} else {
        		return desiredHeight;
        	}
        }
    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hideOnDraw) {
        	setVisibility(View.INVISIBLE);
        	return;
        }
        drawBackground();
    	if (xOffset == -1)
    		xOffset = /*getPaddingLeft() + */getWidth() / 2;
    	if (yOffset == -1)
    		yOffset = getHeight() / 2 + (int)paint.getTextSize() / 2;
        canvas.drawText("[" + pch.ch + "]", xOffset, yOffset, paint);
    }

	public void onClick(View v) {
		Log.d(TAG, v + ".onClick()");
		pch.movePosState();	//if state really changes - onStateChanged will be notified
	}
	
	public void onPosCharStateChanged(PosChar pch, ENCharState newState) {
		if (this.pch != pch)
			return;
//		invalidate();
		startAnimation(anim);
	}

	private void drawBackground() {
		switch (pch.state) {
		case NONE:
			setBackgroundResource(R.drawable.noth);
			break;
		case ABSENT:
			setBackgroundResource(R.drawable.wrong);
			break;
		case PRESENT:
			setBackgroundResource(R.drawable.bull);
			break;
		}
	}

	@Override
	public String toString() {
		return pch + "";
	}

	public void setHideOnDraw(boolean hideOnDraw) {
		this.hideOnDraw = hideOnDraw;
		
	}
}

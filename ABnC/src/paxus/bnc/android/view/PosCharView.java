package paxus.bnc.android.view;

import static paxus.bnc.android.view.CharView.HEIGHT;
import static paxus.bnc.android.view.CharView.WIDTH;
import static paxus.bnc.android.view.CharView.anim;
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
	
	public Paint paint;

	private PosChar pch = PosChar.NULL;
	
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
//		invalidate();
		startAnimation(anim);
	}

	private void drawBackground(Canvas canvas) {
		switch (pch.state) {
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

package paxus.bnc.android;

import paxus.bnc.controller.ICharStateSequencer;
import paxus.bnc.model.Char;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class CharView extends View implements OnClickListener {

	private static final int WIDTH = 14;
	private static final int HEIGHT = 14;
	
	private Paint paint;
	
	private Char ch = Char.NO_ALPHA; 
	public void setCh(Char ch) {
		this.ch = ch;
	}

	public CharView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public CharView(Context context) {
		super(context);
		initView();
	}

	public void initView() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(24);
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Align.CENTER);
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
		ch.moveState(ICharStateSequencer.FORWARD);
	}
}

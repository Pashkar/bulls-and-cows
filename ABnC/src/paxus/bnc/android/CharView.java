package paxus.bnc.android;

import paxus.bnc.model.Char;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CharView extends View {

	private static final int WIDTH = 14;
	private static final int HEIGHT = 14;
	
	private Paint paint;
	
	private Char ch = Char.NULL; 
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
//        setPadding(3, 3, 3, 3);
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }
    
    private int measureWidth(int measureSpec) {
    	/*int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = WIDTH + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;*/
        return WIDTH + getPaddingLeft() + getPaddingRight();
    }

    private int measureHeight(int measureSpec) {
    	/*int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = HEIGHT + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }*/
        return HEIGHT + getPaddingLeft() + getPaddingRight();
    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawText(ch.asString, (float)getPaddingLeft(), (float)getPaddingTop(), paint);
        canvas.drawText(ch.asString, 15, 30, paint);
//        canvas.drawCircle(30, 30, 25, paint);
    }
}

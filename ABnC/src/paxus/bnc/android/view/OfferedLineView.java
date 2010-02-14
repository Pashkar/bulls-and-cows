package paxus.bnc.android.view;

import paxus.bnc.model.Run.WordCompared;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class OfferedLineView extends LinearLayout {
	
	private static final String TAG = "CharLine";
	private WordCompared wc;
	
	public OfferedLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public OfferedLineView(Context context) {
		super(context);
	}

	/*protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(MeasureSpec.makeMeasureSpec(MeasureSpec.EXACTLY, 12), 
						getChildMeasureSpec(heightMeasureSpec, 0, 0));
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }
     
    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        Log.d(TAG, "measureWidth: specSize = " + specSize + ", specMode = " + specMode + (specMode == MeasureSpec.EXACTLY ? " (MeasureSpec.EXACTLY)" : ""));
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

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
	}*/
    
    
    
/*    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        Log.d(TAG, "measureHeight: specSize = " + specSize + ", specMode = " + specMode + (specMode == MeasureSpec.EXACTLY ? " (MeasureSpec.EXACTLY)" : ""));
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            return specSize;
        } else {
        	if (true) return 12;
        	int desiredHeight = getLayoutParams().height + getPaddingTop() + getPaddingBottom();
        	if (specMode == MeasureSpec.AT_MOST) {
        		return desiredHeight < specSize ? desiredHeight : specSize;
        	} else {
        		return desiredHeight;
        	}
        }
    }*/
}

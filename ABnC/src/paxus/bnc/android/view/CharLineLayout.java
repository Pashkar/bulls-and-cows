package paxus.bnc.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class CharLineLayout extends LinearLayout {

	private int wordLength;

	public CharLineLayout(Context context) {
		super(context);
	}

	public CharLineLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setWordLength(int wordLength) {
		this.wordLength = wordLength;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		//width - distribute children evenly 
		measureChildren(getChildMeasureSpec(widthMeasureSpec, 0, width / (wordLength > 0 ? wordLength : 1)),
				getChildMeasureSpec(heightMeasureSpec, 0, height));
		View ch = getChildAt(0);
		if (ch != null && wordLength > 0)
			setMeasuredDimension(ch.getMeasuredWidth() * wordLength, height);
		else 
			setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		final int viewPaddingLeft = getPaddingLeft();
		final int viewPaddingTop = getPaddingTop();
		
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childMeasuredWidth = child.getMeasuredWidth();
				int previousChildWidth = childMeasuredWidth * (i);
				child.layout(
                		viewPaddingLeft + previousChildWidth, 
                		viewPaddingTop,
                        viewPaddingLeft + previousChildWidth + childMeasuredWidth,
                        viewPaddingTop + child.getMeasuredHeight()
                        );
            }
        }
	}
}

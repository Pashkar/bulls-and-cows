package paxus.bnc.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 *	Designed to distribute child evenly by stretching child to fill parent.
 *	
 *	Currently not used 
 */
public class CharLineLayout extends LinearLayout {

//	private int wordLength;

	public CharLineLayout(Context context) {
		super(context);
	}

	public CharLineLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	//TODO create new activity and check out how charView and posLine view are animated. Try to zoom in charView when pressed
	
	
/*	private LinearLayout hidePosLine(Character ch) {
		final LinearLayout line = (LinearLayout) posTableLayout.findViewWithTag(ch);
		if (line == null)
			return null;
		line.setLayoutAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationEnd(Animation animation) {
				posTableLayout.removeView(line);
			}
		});
		
		//TODO move animation logic into custom layout class
		line.setLayoutAnimation(layoutOutAnimation);
		for (int i = 0; i < line.getChildCount(); i++)	//hide child after layout animation+ 
			((PosCharView)line.getChildAt(i)).setHideOnDraw(true);
		line.invalidate();	//start layout animation

		posTableLayout.removeView(line);
		
		//TODO - try to add transition animation - soft disappearing for row
		return line;
	}*/

	/*public void setWordLength(int wordLength) {
		this.wordLength = wordLength;
	}

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
	}*/
}

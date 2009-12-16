package paxus.bnc.android.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class TestLinearLayout extends LinearLayout {

	private static final String TAG = "TestLinearLayout";

	public TestLinearLayout(Context context) {
		super(context);
		Log.v(TAG, "<init>");
	}

	public TestLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.v(TAG, "<init>");
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		boolean res = true;
		Log.v(TAG, "onInterceptTouchEvent, event = " + event + ", res = " + res);
		return res; //super.onInterceptTouchEvent(event);
		//TODO Scroll?
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.v(TAG, "onTouchEvent, event = " + event + ", res = " + true);
		//TODO find child by coords and performClick();
		
		for (int i = 0; i < getChildCount(); i++) {
			Rect r = new Rect();
			 
//			r.offset(0, -getTop());
//			Log.v(TAG, r.toShortString());
			boolean hit = event.getX() >= r.left && event.getX() < r.right;
			if (hit)
				Log.v(TAG, "i = " + i + ", " + hit);
		}
		
		return true;
	}

}

package paxus.bnc.android.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.AbsListView.OnScrollListener;

public class TracingGridView extends GridView implements OnScrollListener {

	private static final String TAG = "TracingGridView";

	public TracingGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TracingGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnScrollListener(this);
	}

	public TracingGridView(Context context) {
		super(context);
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
		boolean b = false;
		Log.v(TAG, "onInterceptTouchEvent, event = " + event + ", b = " + b);
		Rect r = new Rect();
		View c = getChildAt(0);
		c.getGlobalVisibleRect(r);
		Log.d(TAG, "view = " + c + ", r = " + r.toShortString());
		Log.d(TAG, "grid top = " + getTop() + ", scrollY = " + getScrollY()+  ", res = " + (r.top - getTop() + getScrollY()));
		return b;
		//return super.onInterceptTouchEvent(event);
		//TODO Scroll?
	}

	
	
	public boolean onTouchEvent(MotionEvent event) {
//		Log.v(TAG, "onTouchEvent, event = " + event);
		
		return super.onTouchEvent(event);
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		Log.v(TAG, "onScroll, firstVisibleItem = " + firstVisibleItem);
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		Log.v(TAG, "onScrollStateChanged, scrollState = " + scrollState);
	}
}

package paxus.bnc.android.view;

import paxus.bnc.android.R;
import paxus.bnc.model.WordComparisonResult;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ComparisonResultView extends View {

	private static final String TAG = "ComparisonResultView";
	private WordComparisonResult result;
	
	private static final Paint bullPaint = new Paint();
	private static final Paint cowPaint = new Paint();
	
	private static int top;
	private static int bottom = -1;
	private static int halfTop = -1;
	private static int width = -1;
	private static int step = -1;

	public ComparisonResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ComparisonResultView(Context context) {
		super(context);
	}

	public void setResult(WordComparisonResult result) {
		this.result = result;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (result == null)
			return;
		if (width == -1)
			init();
		
		int off = width - step;
		for (int i = 0; i < result.bullsCount; i++) {
//			Log.d(TAG, "step = " + step + ", height = " + height + ", width = " + width);
			canvas.drawLine(off, top, off, bottom, bullPaint);
			off -= step;
		}
		
		for (int i = 0; i < result.cowsCount; i++) {
			canvas.drawLine(off, halfTop, off, bottom, cowPaint);
			off -= step;
		}
	}

	private void init() {
		int height = getHeight();
		bottom = height * 4 / 5;
		top = height / 5;
		halfTop = height * 2 / 5;
		
		width = getWidth();
		step = width / 7/*Main.run.wordLength*/;
		
		bullPaint.setColor(getResources().getColor(R.drawable.bull_color));
		cowPaint.setColor(getResources().getColor(R.drawable.cow_color));
		int paintWidth = step / 2;
		bullPaint.setStrokeWidth(paintWidth);
		cowPaint.setStrokeWidth(paintWidth);
	}
}

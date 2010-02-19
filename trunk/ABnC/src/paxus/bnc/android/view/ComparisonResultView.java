package paxus.bnc.android.view;

import paxus.bnc.android.Main;
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
	private static final Paint nothPaint = new Paint(); 
	
	private static int left;
	private static int top;
	private static int right;
	private static int bottom = -1;
	private static int halfTop = -1;
	private static int width = -1;
	private static int step = -1;
	private static int paintWidth;

	public ComparisonResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ComparisonResultView(Context context) {
		super(context);
	}

	public void setResult(WordComparisonResult result) {
		this.result = result;
	}
	
	public static void reset() {
		width = -1;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (result == null)
			return;
		
		if (width == -1) 
			init();
		
		int off = right - step / 2 - paintWidth / 2;
		if (result.bullsCount + result.cowsCount > 0) {
			for (int i = 0; i < result.bullsCount; i++) {
				canvas.drawLine(off, top, off, bottom, bullPaint);
				off -= step;
			}
			
			for (int i = 0; i < result.cowsCount; i++) {
				canvas.drawLine(off, halfTop, off, bottom, cowPaint);
				off -= step;
			}
		} else {
			canvas.drawLine(left + paintWidth, halfTop, right - paintWidth, halfTop, nothPaint);
		}
	}

	private void init() {
		int height = getHeight();
		bottom = height * 4 / 5;
		top = height / 5;
		halfTop = height / 2;
		
		width = getWidth() - getPaddingLeft() - getPaddingRight();
		right = getWidth() - getPaddingRight(); 
		left = getPaddingLeft();
		step = width / /*7 */Main.run.wordLength;
		
		bullPaint.setColor(getResources().getColor(R.drawable.bull_color));
		cowPaint.setColor(getResources().getColor(R.drawable.cow_color));
		nothPaint.setColor(getResources().getColor(R.drawable.noth_color));
		
		paintWidth = /*step */ (width / 7) * 2 / 3;
		bullPaint.setStrokeWidth(paintWidth);
		cowPaint.setStrokeWidth(paintWidth);
		nothPaint.setStrokeWidth(paintWidth);
	}
}

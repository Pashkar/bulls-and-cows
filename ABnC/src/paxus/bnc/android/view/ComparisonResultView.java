package paxus.bnc.android.view;

import paxus.bnc.model.WordComparisonResult;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ComparisonResultView extends View {

	private Paint paint;
	private WordComparisonResult result;

	public ComparisonResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ComparisonResultView(Context context) {
		super(context);
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	public void setResult(WordComparisonResult result) {
		this.result = result;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (result == null)
			return;
		String str = result.bullsCount + " : " + result.cowsCount;
		canvas.drawText(str, getPaddingLeft() + getWidth() / 2, getPaddingTop() + getHeight(), paint);
	}
}

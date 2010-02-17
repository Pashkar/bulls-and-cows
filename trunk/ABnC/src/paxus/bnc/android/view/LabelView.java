package paxus.bnc.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LabelView extends View {

	public Character caption;
	public Paint paint;
	private static int xOffset = -1;
	private static int yOffset = -1;
	
	public LabelView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
        if (caption != null && paint != null) {
        	if (xOffset == -1)
        		xOffset = /*getPaddingLeft() + */getWidth() / 2;
        	if (yOffset == -1)
        		yOffset = getHeight() / 2 + (int)paint.getTextSize() / 2;
			canvas.drawText("" + caption, xOffset, yOffset, paint);
		}
	}
	
}

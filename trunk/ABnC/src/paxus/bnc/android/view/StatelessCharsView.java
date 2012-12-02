package paxus.bnc.android.view;

import paxus.bnc.model.Char;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class StatelessCharsView extends CharView {
	private static final String ATTR_CAPTION = "caption";
	private final String caption;

	public StatelessCharsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		caption = context.getResources().getString(attrs.getAttributeResourceValue(null, ATTR_CAPTION, -1));
		stateless = true;
		changeStateOnClick = false;
	}

	@Override
	protected void doDrawCaption(Canvas canvas) {
		canvas.drawText(caption, xOffset, yOffset, paint);
	}

	@Override
	public void setViewPos(int pos) {
		return;
	}
	
	@Override
	public void setChar(Char ch) {
		return;
	}
	
	@Override
	public void setChar(Char ch, boolean posMatched) {
		return;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + caption + "]";
	}
}

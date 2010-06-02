package paxus.bnc.android.view;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.Log;

public class AlarmBorder extends ShapeDrawable {
	
	private static final String TAG = "AlarmBorder";

	private static final int COLOR = 0xFF000000;
	private static final int SIDE = 4;
	private static final int RADIUS = 2;
	private static final float[] OUTER = new float[] {SIDE, SIDE, SIDE, SIDE, SIDE, SIDE, SIDE, SIDE};
	private static final RectF INNNER = new RectF(RADIUS, RADIUS, RADIUS, RADIUS);
	private static final Shape SHAPE = new RoundRectShape(OUTER, INNNER, null);
	
	public AlarmBorder() {
		super(SHAPE);
		getPaint().setColor(COLOR);
		Log.v(TAG, this + " const");
	}
	
	public void init(int width, int height) {
		Log.v(TAG, this + " init: " + width + "x" + height);
		setBounds(0, 0, width, height);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		Log.v(TAG, this + " draw");
	}
}

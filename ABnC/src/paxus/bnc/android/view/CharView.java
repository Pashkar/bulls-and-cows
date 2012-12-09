package paxus.bnc.android.view;

import paxus.bnc.BncException;
import paxus.bnc.android.Main;
import paxus.bnc.android.R;
import paxus.bnc.controller.ICharStateChangedListener;
import paxus.bnc.controller.IPosCharStateChangedListener;
import paxus.bnc.model.Char;
import paxus.bnc.model.ENCharState;
import paxus.bnc.model.PosChar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class CharView extends View implements OnClickListener, ICharStateChangedListener, 
		IPosCharStateChangedListener {

	private static final String TAG = "CharView";
	private static final String ATTR_STATELESS = "stateless";
	private static final String ATTR_COLOR = "color";
	private static final String ATTR_CHANGE_STATE_ON_CLICK = "changeStateOnClick";
	
	static Animation animFade;
	protected final Paint paint; 

	private Char ch = Char.NULL;
	
	public boolean changeStateOnClick = true;
	public boolean stateless = false;	//should we react on clicks, should we redraw background based on state?
	
	/**
	 *	For "bull" when position of CharView pointed. Stores for the CharView instance position of it in word.
	 *	Compared to position in PosTable, marked by user 
	 */
	private int viewPos = -1;	
	
	private boolean posMatched = false;
	protected int xOffset = -1;
	protected int yOffset = -1;
	private OnClickListener clickListener;
	
	public CharView(Context context, AttributeSet attrs) {
		super(context, attrs);
		changeStateOnClick = attrs.getAttributeBooleanValue(null, ATTR_CHANGE_STATE_ON_CLICK, true);
		stateless = attrs.getAttributeBooleanValue(null, ATTR_STATELESS, false);
		if (animFade == null)
			animFade = AnimationUtils.loadAnimation(context, R.anim.char_fade_in_anim);
		String colorAttr = attrs.getAttributeValue(null, ATTR_COLOR);
		paint = Main.getPaint(colorAttr == null ? 0 /*not set - use default*/ :		 
			getResources().getIdentifier(colorAttr, null, Main.class.getPackage().getName()));
		super.setOnClickListener(this);
	}

	public void setChar(Char ch, boolean posMatched) {
		this.posMatched = posMatched;
		setChar(ch);
	}
	
	public void setChar(Char ch) {
		this.ch = ch;
		ch.addStateChangedListener(this);
//		invalidate();
		setBackground();
		startAnimation(animFade);
	}

	public Char getChar() {
		return ch;
	}
	
	public void resetChar() {
		ch.removeStateChangedListener(this);
		ch = Char.NULL;
		posMatched = false;
		clickListener = null;
//		invalidate();
		setBackground();
		startAnimation(animFade);
	}
	
	/**
	 *	For "bull" when position of CharView pointed. Stores for the CharView instance position of it in word.
	 *	Compared to position in PosTable, marked by user 
	 */
	public void setViewPos(int pos) {
		this.viewPos = pos;
//		invalidate();
	}

    @Override
	protected void onDraw(Canvas canvas) {
    	initPaintOffset();
        setBackground();
        if (paint != null)
    		doDrawCaption(canvas);
    }

	protected void doDrawCaption(Canvas canvas) {
		if (ch != null)
			canvas.drawText("" + ch.ch, xOffset, yOffset, paint);
	}

	private void initPaintOffset() {
		if (xOffset == -1)
			xOffset = /*getPaddingLeft() + */ getWidth() / 2;
		if (yOffset == -1)
			yOffset = getHeight() / 2 + (int)paint.getTextSize() / 2;
	}
	
	private void setBackground() {
		if (ch == null)
			return;
		if (stateless)
			return;
		switch (ch.getState()) {
		case NONE:
			setBackgroundResource(R.drawable.noth);
			break;
		case ABSENT:
			setBackgroundResource(R.drawable.wrong);
			break;
		case PRESENT:
			setBackgroundResource(posMatched ? R.drawable.bull : R.drawable.cow);
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		try {
			Log.d(TAG, v + ".onClick()");
			if (clickListener != null)
				clickListener.onClick(v);
			
			startAnimation(animFade);
			if (changeStateOnClick) 
				ch.moveState();		//if state really changes - onStateChanged will be notified to call onDraw()
		} catch (BncException e) {};
	}

	@Override
	public void onCharStateChanged(Character ch, ENCharState newState) {
//		invalidate();
		startAnimation(animFade);
	}

	@Override
	public void onPosCharStateChanged(PosChar ch, ENCharState newState) {
		boolean oldMatched = posMatched;
		if (viewPos != -1 && ch.ch == this.ch.ch && ch.pos == viewPos)
			posMatched = (newState == ENCharState.PRESENT);
		if (oldMatched != posMatched)
//			invalidate();
			startAnimation(animFade);
	}
	
	@Override
	public String toString() {
		return ch + "(" + viewPos + ")";
	}

	/** 
	 * CharView is always OnClickListener for itself. External objects may add additional listeners and 
	 * event would be replicated (android.view.View supports only one listener and for CharView it is occupied by itself).
	 * Currently only one external listener supported - to be widened when needed.
	 */
	@Override
	public final void setOnClickListener(OnClickListener l) {
		clickListener = l;
	}
}
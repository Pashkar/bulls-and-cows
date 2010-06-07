package paxus.bnc.android.hint;

import paxus.bnc.android.Main;
import paxus.bnc.android.R;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

//immutable
public class Hint {
	private static final int OFFSET_DELAY = 3000;
	private static final int TOGGLE_PERIOD = 500;

	public static Hint GUESS = new Hint(R.string.hint_guess);
	
	private static final String TAG = "Hint";
	
	private final static Handler handler = new Handler();
	private final String hintMsg;
	private final Toast toast;
//	private final AlarmBorder border = new AlarmBorder();
	private IHintView view;
	private Runnable actTask;
	private boolean active = false;

	private boolean initialized = false;
	
	private Hint(int hintMsgId) {
		this.hintMsg = Main.context.getResources().getString(hintMsgId);
		this.toast = Toast.makeText(Main.context, hintMsgId, Toast.LENGTH_LONG);
	}
	
	public Hint createInstance(IHintView view) {
		this.view = view;
		this.initialized = true;
		this.actTask = new Runnable() {
			public void run() {
				Hint.this.view.doHint();
				if (active)
					handler.postDelayed(actTask, TOGGLE_PERIOD);	//reschedule the action
			}
		};
		return this;
	}
	
	public synchronized Hint start() {
		if (!initialized)
			throw new RuntimeException("invoke .createInstance() first");
		Log.d(TAG, "Schedule for \"" + view + "\"");
		active = true;
		handler.postDelayed(new Runnable() {
			public void run() {
				if (!active)
					return;
				Log.d(TAG, "Run for \"" + hintMsg + "\"");
				toast.show();
//				border.init(view.getWidth(), view.getHeight());	//nowhere earlier getWidth/Height are available
//				view.setBorder(border);
//				view.setBorderVisible(true);
				handler.postDelayed(actTask, TOGGLE_PERIOD);
			}
		}, OFFSET_DELAY);
		return this;
	}
	
	public synchronized void stop() {
		active = false;
		handler.removeCallbacks(actTask);
//		view.setBorderVisible(false);
	}
}

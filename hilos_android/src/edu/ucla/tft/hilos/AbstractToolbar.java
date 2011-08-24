package edu.ucla.tft.hilos;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Abstract super class for showing a floating toolbar.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public abstract class AbstractToolbar extends FrameLayout {
	private final int[] mButton2Id;

	public static interface Listener {
		public static final int ACTION_DOWN = 0, ACTION_UP = 1,
				ACTION_SELECTED = 2;

		public void onButtonAction(AbstractToolbar toolbar, int button,
				int action);
	}

	private Listener mListener;

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v instanceof Button) {
				int id = ((Button) v).getId();
				for (int i = 0; i < mButton2Id.length; i++) {
					if (mButton2Id[i] == id) {
						notifyButton(i, Listener.ACTION_SELECTED);
					}
				}
			}
		}
	};

	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (v instanceof Button) {
				int id = ((Button) v).getId();
				for (int i = 0; i < mButton2Id.length; i++) {
					if (mButton2Id[i] == id) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							notifyButton(i, Listener.ACTION_DOWN);
							break;
						case MotionEvent.ACTION_UP:
							notifyButton(i, Listener.ACTION_UP);
							break;
						}
					}
				}
			}
			return false;
		}
	};

	public AbstractToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mButton2Id = getButtonToIdMap();
		View view = LayoutInflater.from(context).inflate(getToolbarResId(),
				null);
		addView(view);
		for (int id : mButton2Id) {
			((Button) view.findViewById(id))
					.setOnClickListener(mOnClickListener);
			((Button) view.findViewById(id))
					.setOnTouchListener(mOnTouchListener);
		}
	}

	protected abstract int getToolbarResId();

	protected abstract int[] getButtonToIdMap();

	public boolean isButtonEnabled(int button) {
		if (button < 0 || button > mButton2Id.length) {
			return false;
		}
		Button b = (Button) findViewById(mButton2Id[button]);
		return b != null && b.isEnabled();
	}

	public void setButtonEnabled(int button, boolean enabled) {
		if (button < 0 || button > mButton2Id.length) {
			return;
		}
		Button b = (Button) findViewById(mButton2Id[button]);
		if (b != null) {
			b.setEnabled(enabled);
		}
	}

	public boolean isButtonSelected(int button) {
		if (button < 0 || button > mButton2Id.length) {
			return false;
		}
		Button b = (Button) findViewById(mButton2Id[button]);
		return b != null && b.isSelected();
	}

	public void setButtonSelected(int button, boolean enabled) {
		if (button < 0 || button > mButton2Id.length) {
			return;
		}
		Button b = (Button) findViewById(mButton2Id[button]);
		if (b != null) {
			b.setSelected(enabled);
		}
	}

	public Listener getListener() {
		return mListener;
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	protected void notifyButton(int button, int action) {
		if (mListener != null) {
			mListener.onButtonAction(this, button, action);
		}
	}
}

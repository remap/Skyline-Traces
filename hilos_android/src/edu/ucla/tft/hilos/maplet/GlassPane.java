package edu.ucla.tft.hilos.maplet;

import edu.ucla.tft.hilos.BaseActivity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * A transparent overlay which supports drawing a selection marquee.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class GlassPane extends View {
	public static interface Listener {
		public void selectionRectChanged(Rect rect);
	}

	private static final int PS_OFF = 0, PS_ON = 1;

	private int mPinState = PS_OFF;
	private int mLastPinState = mPinState;
	private Paint mMarqueeFillPaint;
	private Paint mMarqueeStrokePaint;
	private Paint mMarkerHighlightPaint;
	private float mMarkerHighlightProgress;
	private Listener mListener;
	private View mDispatchTouchEventsTo;

	public GlassPane(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GlassPane(Context context) {
		super(context);
		init();
	}

	private void init() {
		Resources res = getResources();

		mMarqueeFillPaint = new Paint();
		mMarqueeFillPaint.setColor(0x80ff0000);
		mMarqueeFillPaint.setStyle(Style.FILL);

		mMarqueeStrokePaint = new Paint();
		mMarqueeStrokePaint.setColor(Color.BLACK);
		mMarqueeStrokePaint.setStyle(Style.STROKE);
		mMarqueeStrokePaint.setStrokeWidth(3.0f);

		mMarkerHighlightPaint = new Paint();
		mMarkerHighlightPaint.setColor(0x80ff0000);
	}

	public Listener getListener() {
		return mListener;
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	public View getDispatchTouchEventsTo() {
		return mDispatchTouchEventsTo;
	}

	public void setDispatchTouchEventsTo(View view) {
		this.mDispatchTouchEventsTo = view;
	}

	private abstract class PositionHighlightAnimation implements Runnable {
		private int count;
		private int steps = 10;
		private int repeatCount;

		public PositionHighlightAnimation(int repeatCount) {
			this.repeatCount = repeatCount;
		}

		public abstract void tick(float t);

		public abstract void end();

		private long last;

		@Override
		public void run() {
			++count;
			postInvalidate();
			long time = System.nanoTime();
			long diff_ms = (time - last) / 1000000;
			last = time;
			if (count < steps) {
				tick((float) count / steps);
				postDelayed(this, Math.max(0, 100 - diff_ms));
			} else if (--repeatCount > 0) {
				count = 0;
				postDelayed(this, Math.max(0, 100 - diff_ms));
			} else {
				end();
			}
		}
	}

	public void startMarkerHighlightAnimation() {
		post(new PositionHighlightAnimation(2) {
			@Override
			public void tick(float t) {
				mMarkerHighlightProgress = t;
				postInvalidate();
			}

			@Override
			public void end() {
				mMarkerHighlightProgress = -1;
				postInvalidate();
			}
		});
	}

	private int mx, my;
	private int sx, sy;
	private int pinx = 100, piny = 100;

	private class DelayedPinStateOnSetter implements Runnable {
		private boolean canceled;

		public void cancel() {
			canceled = true;
		}

		@Override
		public void run() {
			if (canceled) {
				return;
			}
			mPinState = PS_ON;
			pinx = sx;
			piny = sy;
			startMarkerHighlightAnimation();
		}
	}

	private DelayedPinStateOnSetter mDelayedPinStateOnSetter;

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		mx = (int) event.getX();
		my = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			sx = mx;
			sy = my;
			if (mDelayedPinStateOnSetter != null) {
				mDelayedPinStateOnSetter.cancel();
				mDelayedPinStateOnSetter = null;
			}
			postDelayed(
					(mDelayedPinStateOnSetter = new DelayedPinStateOnSetter()),
					1000);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mPinState == PS_OFF) {
				if (mDelayedPinStateOnSetter != null
						&& (Math.abs(mx - sx) > 3 || Math.abs(my - sy) > 3)) {
					mDelayedPinStateOnSetter.cancel();
					mDelayedPinStateOnSetter = null;
				}
			} else if (mPinState == PS_ON) {
				updateSelectionRect();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mPinState == PS_OFF) {
				if (mDelayedPinStateOnSetter != null) {
					mDelayedPinStateOnSetter.cancel();
					mDelayedPinStateOnSetter = null;
				}
			} else if (mPinState == PS_ON) {
				mPinState = PS_OFF;
				if (mListener != null) {
					mListener.selectionRectChanged(mSelectionRect);
				}
				((BaseActivity) getContext()).playEarcon(BaseActivity.EC_TOCK);
				updateSelectionRect();
			}
			break;
		}
		postInvalidate();
		if (mPinState == PS_OFF) {
			if (mDispatchTouchEventsTo != null) {
				mDispatchTouchEventsTo.dispatchTouchEvent(event);
			}
		} else if (mLastPinState == PS_OFF) {
			if (mDispatchTouchEventsTo != null) {
				MotionEvent e = MotionEvent.obtain(event);
				e.setAction(MotionEvent.ACTION_UP);
				mDispatchTouchEventsTo.dispatchTouchEvent(e);
			}
		}
		mLastPinState = mPinState;
		return true;
	}

	private Rect mSelectionRect = new Rect();

	private void updateSelectionRect() {
		if (mPinState == PS_ON) {
			mSelectionRect.left = Math.min(pinx, mx);
			mSelectionRect.top = Math.min(piny, my);
			mSelectionRect.right = Math.max(pinx, mx);
			mSelectionRect.bottom = Math.max(piny, my);
		} else {
			mSelectionRect.setEmpty();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mMarkerHighlightProgress > 0) {
			canvas.drawCircle(pinx, piny, 100 * mMarkerHighlightProgress,
					mMarkerHighlightPaint);
		}
		if (mPinState == PS_ON) {
			canvas.drawRect(mSelectionRect, mMarqueeFillPaint);
			canvas.drawRect(mSelectionRect, mMarqueeStrokePaint);
		}
	}
}

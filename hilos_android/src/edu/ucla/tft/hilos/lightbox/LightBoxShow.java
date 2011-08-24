package edu.ucla.tft.hilos.lightbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.widget.FrameLayout;
import edu.ucla.tft.hilos.BaseActivity;

/**
 * A view to display a {@link LightBoxItem}, which could an image, an audio or a
 * video file. Also recognizes gestures to view full screen and manipulate the
 * media item.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class LightBoxShow extends FrameLayout implements
		LightBoxItemContainer.Listener {
	private static final int MIN_GESTURE_DISTANCE = 30;
	private LightBoxItemContainer mContainer;

	protected Paint mPaint = new Paint();
	protected RectF mViewRect, mDefaultViewRect;

	private LightBoxItem mSelectedItem;
	private GestureDetector mGestureDetector;
	private OnTouchListener mControlsTouchListener;

	private boolean mControlsEnabled = false;
	private double mControlsScale = 1.0;
	private Point mControlsOffset = new Point();

	public LightBoxShow(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);
		setBackgroundColor(Color.TRANSPARENT);

		mGestureDetector = new GestureDetector(getContext(),
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						doSingleTap();
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						doDoubleTap();
						return true;
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						float dx = e2.getX() - e1.getX();
						float dy = e2.getY() - e1.getY();
						float adx = Math.abs(dx);
						float ady = Math.abs(dy);
						if (adx >= ady && adx >= MIN_GESTURE_DISTANCE) {
							if (dx < 0) {
								doLeft();
							} else {
								doRight();
							}
							return true;
						} else if (ady >= MIN_GESTURE_DISTANCE) {
							if (dy < 0) {
								doUp();
							} else {
								doDown();
							}
							return true;
						}
						return true;
					}
				});
		mControlsTouchListener = new OnTouchListener() {
			private int sx, sy;
			private int mx, my;
			private int pmx, pmy;
			private ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(
					getContext(), new OnScaleGestureListener() {
						@Override
						public void onScaleEnd(ScaleGestureDetector detector) {
							adjustViewRect();
						}

						@Override
						public boolean onScaleBegin(
								ScaleGestureDetector detector) {
							return true;
						}

						@Override
						public boolean onScale(ScaleGestureDetector detector) {
							scaleViewRect(detector.getScaleFactor());
							return true;
						}
					});

			private GestureDetector mGestureDetector = new GestureDetector(
					getContext(),
					new GestureDetector.SimpleOnGestureListener() {
						@Override
						public boolean onSingleTapUp(MotionEvent e) {
							doSingleTap();
							return true;
						}
					});

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mScaleGestureDetector.onTouchEvent(event);
				if (event.getPointerCount() == 1) {
					mGestureDetector.onTouchEvent(event);
					mx = (int) event.getX();
					my = (int) event.getY();
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						sx = pmx = mx;
						sy = pmy = my;
						break;
					case MotionEvent.ACTION_MOVE:
						translateViewRect(mx - pmx, my - pmy);
						break;
					case MotionEvent.ACTION_UP:
						adjustViewRect();
						break;
					}
					pmx = mx;
					pmy = my;
				}
				return true;
			}
		};
	}

	public boolean isControlsEnabled() {
		return mControlsEnabled;
	}

	public void setControlsEnabled(boolean controlsEnabled) {
		this.mControlsEnabled = controlsEnabled;
	}

	public LightBoxItemContainer getLightBoxItemContainer() {
		return mContainer;
	}

	public void setLightBoxItemContainer(LightBoxItemContainer container) {
		if (mContainer != null) {
			mContainer.removeListener(this);
		}
		mContainer = container;
		mContainer.addListener(this);
		postInvalidate();
	}

	protected void doSingleTap() {
		if (mContainer != null) {
			mContainer.onLaunchGesture();
		}
	}

	protected void doDoubleTap() {
	}

	protected void doLeft() {
		if (mContainer != null) {
			((BaseActivity) getContext()).playEarcon(BaseActivity.EC_FLIP);
			int selected = mContainer.getLightBoxItemSelected();
			int total = mContainer.getLightBoxItemsCount();
			mContainer.setLightBoxItemSelected((selected + 1) % total);
		}
	}

	protected void doRight() {
		if (mContainer != null) {
			((BaseActivity) getContext()).playEarcon(BaseActivity.EC_FLIP);
			int selected = mContainer.getLightBoxItemSelected();
			int total = mContainer.getLightBoxItemsCount();
			mContainer.setLightBoxItemSelected((selected - 1 + total) % total);
		}
	}

	protected void doUp() {
	}

	protected void doDown() {
	}

	@Override
	public void selectionChanged() {
		LightBoxItem item;
		if (mContainer == null
				|| (item = mContainer.getLightBoxItemAt(mContainer
						.getLightBoxItemSelected())) == null) {
			mSelectedItem = null;
			return;
		}
		mSelectedItem = item;
		mViewRect = null;
		postInvalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isControlsEnabled()) {
			if (mSelectedItem.isImageType()
					&& mControlsTouchListener.onTouch(this, event)) {
				return true;
			}
		} else if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}
		return true; // consume event
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (mContainer == null) {
			return;
		}
		Object data;
		if (mSelectedItem == null || (data = mSelectedItem.getData()) == null) {
			return;
		}
		if (mSelectedItem.isImageType()) {
			BitmapDrawable bd = (BitmapDrawable) data;
			Bitmap bitmap = bd.getBitmap();
			if (bitmap != null) {
				if (mViewRect == null) {
					mViewRect = new RectF(calcDefaultViewRect(bitmap, true));
				}
				bd.setBounds((int) mViewRect.left, (int) mViewRect.top,
						(int) mViewRect.right, (int) mViewRect.bottom);
				bd.draw(canvas);
			}
		} else if (mSelectedItem.isAudioType()) {
			Bitmap bitmap = mSelectedItem.getThumbnail(getContext());
			if (bitmap != null) {
				if (mViewRect == null) {
					mViewRect = new RectF(calcDefaultViewRect(bitmap, false));
				}
				canvas.drawBitmap(bitmap, mViewRect.left, mViewRect.top, mPaint);
			}
		} else if (mSelectedItem.isVideoType()) {
			Bitmap bitmap = mSelectedItem.getThumbnail(getContext());
			if (bitmap != null) {
				if (mViewRect == null) {
					mViewRect = new RectF(calcDefaultViewRect(bitmap, false));
				}
				canvas.drawBitmap(bitmap, mViewRect.left, mViewRect.top, mPaint);
			}
		}
	}

	private int isValid(RectF r, int width, int height) {
		if (r.width() < width && r.height() < height) {
			return 1;
		} else if (r.left < 0 && r.right < width) {
			return 2;
		} else if (r.left > 0 && r.right > width) {
			return 3;
		} else if (r.top < 0 && r.bottom < height) {
			return 4;
		} else if (r.top > 0 && r.bottom > height) {
			return 5;
		}
		return 0;
	}

	private void adjustViewRect() {
		int width = getWidth(), height = getHeight();
		int valid;
		for (int i = 0; i < 3; i++) {
			if ((valid = isValid(mViewRect, width, height)) != 0) {
				switch (valid) {
				case 1:
					mViewRect.set(mDefaultViewRect);
					break;
				case 2:
					mViewRect.offset(-mViewRect.left, 0);
					break;
				case 3:
					mViewRect.offset(width - mViewRect.right, 0);
					break;
				case 4:
					mViewRect.offset(0, -mViewRect.top);
					break;
				case 5:
					mViewRect.offset(0, height - mViewRect.bottom);
					break;
				}
			}
		}
		postInvalidate();
	}

	private void translateViewRect(int dx, int dy) {
		mViewRect.offset(dx, dy);
		postInvalidate();
	}

	private void scaleViewRect(float scale) {
		int width = getWidth();
		int height = getHeight();
		mViewRect.offset(-width / 2, -height / 2);
		mViewRect.set(mViewRect.left * scale, mViewRect.top * scale,
				mViewRect.right * scale, mViewRect.bottom * scale);
		mViewRect.offset(width / 2, height / 2);
		postInvalidate();
	}

	private RectF calcDefaultViewRect(Bitmap bitmap, boolean stretch) {
		int width = bitmap.getWidth(), height = bitmap.getHeight();
		int viewWidth = getWidth(), viewHeight = getHeight();
		float scale = stretch ? Math.min((float) viewWidth / width,
				(float) viewHeight / height) : 1.0f;
		float viewRectWidth = scale * width;
		float viewRectHeight = scale * height;
		mDefaultViewRect = new RectF();
		mDefaultViewRect.left = (viewWidth - viewRectWidth) / 2;
		mDefaultViewRect.right = mDefaultViewRect.left + viewRectWidth;
		mDefaultViewRect.top = (viewHeight - viewRectHeight) / 2;
		mDefaultViewRect.bottom = mDefaultViewRect.top + viewRectHeight;
		return mDefaultViewRect;
	}
}
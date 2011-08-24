package edu.ucla.tft.hilos.lightbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import edu.ucla.tft.hilos.BaseActivity;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.StyleInfo;
import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;

/**
 * Displays a scrollable list of thumbnails for a set of media items.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class ThumbnailList extends View implements
		LightBoxItemContainer.Listener {
	private static final int MAX_WIDTH = 200;
	private LightBoxItemContainer mContainer;
	private int mOffset;
	private Rect mViewRect = new Rect();
	private Rect[] mThumbRects;
	private int mTotalWidth;
	private Paint mSelectionStrokePaint;
	private Paint mSelectionFillPaint;

	private BitmapDrawable mLoadingIcon;
	private BitmapDrawable mErrorIcon;

	public ThumbnailList(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);
		setBackgroundColor(Color.BLACK);
		mSelectionStrokePaint = new Paint();
		mSelectionStrokePaint.setStyle(Style.STROKE);
		mSelectionStrokePaint.setColor(Color.BLACK);
		mSelectionFillPaint = new Paint();
		mSelectionFillPaint.setStyle(Style.FILL);
		mSelectionFillPaint.setColor(Color.WHITE);

		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		mLoadingIcon = currStyleInfo
				.getBitmapDrawable(StyleBitmapId.ic_loading);
		mErrorIcon = currStyleInfo.getBitmapDrawable(StyleBitmapId.ic_error);
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
		mOffset = 0;
		postInvalidate();
	}

	@Override
	public void selectionChanged() {
		postInvalidate();
	}

	private int mX, mY;
	private int mSx, mSy, mSOffset;
	private boolean mMoved;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mX = (int) event.getX();
		mY = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mSx = mX;
			mSy = mY;
			mSOffset = mOffset;
			break;
		case MotionEvent.ACTION_MOVE:
			mOffset = Math.min(0,
					Math.max(-(mTotalWidth - getWidth()), mSOffset + mX - mSx));
			if (!mMoved && Math.abs(mOffset - mSOffset) > 5) {
				mMoved = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (!mMoved) { // clicked
				if (mThumbRects != null && mContainer != null) {
					for (int i = 0; i < mThumbRects.length; i++) {
						if (mThumbRects[i].contains(mX, mY)) {
							if (mContainer.getLightBoxItemSelected() != i) {
								((BaseActivity) getContext()).playEarcon(BaseActivity.EC_TOCK);
								mContainer.setLightBoxItemSelected(i);
							}
							break;
						}
					}
				}
			}
			mMoved = false;
			break;
		}
		postInvalidate();
		return true;
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (mContainer == null) {
			return;
		}
		int viewWidth = MAX_WIDTH, viewHeight = getHeight();
		int count = mContainer.getLightBoxItemsCount();
		if (mThumbRects == null || mThumbRects.length != count) {
			mThumbRects = new Rect[count];
			for (int i = 0; i < count; i++) {
				mThumbRects[i] = new Rect();
			}
		}
		int selection = mContainer.getLightBoxItemSelected();
		int x = 0;
		for (int i = 0; i < count; i++) {
			LightBoxItem item = mContainer.getLightBoxItemAt(i);
			BitmapDrawable d;
			if (item == null) {
				d = mLoadingIcon;
			} else {
				d = new BitmapDrawable(item.getThumbnail(getContext()));
			}
			Bitmap bitmap = d.getBitmap();
			if (bitmap == null) {
				d = mErrorIcon;
				bitmap = d.getBitmap();
			}
			int width = bitmap.getWidth(), height = bitmap.getHeight();
			double mScaleView = Math.min((double) (viewWidth - 20) / width,
					(double) (viewHeight - 20) / height);
			int viewRectWidth = (int) (mScaleView * width);
			int viewRectHeight = (int) (mScaleView * height);
			mViewRect.left = x + mOffset + 10;
			mViewRect.right = mViewRect.left + viewRectWidth;
			mViewRect.top = (viewHeight - viewRectHeight) / 2;
			mViewRect.bottom = mViewRect.top + viewRectHeight;
			if (selection == i) {
				canvas.drawRect(x + mOffset, 0, x + mOffset + viewRectWidth
						+ 20, getHeight(), mSelectionFillPaint);
				// canvas.drawRect(mViewRect, mSelectionPaint);
			}
			mThumbRects[i].set(mViewRect);
			if (mThumbRects[i].left > 2 * getWidth()) {
				break;
			}
			d.setBounds(mViewRect);
			d.draw(canvas);
			canvas.drawRect(mViewRect, mSelectionStrokePaint);
			x += viewRectWidth + 20;
		}
		mTotalWidth = x;
	}
}
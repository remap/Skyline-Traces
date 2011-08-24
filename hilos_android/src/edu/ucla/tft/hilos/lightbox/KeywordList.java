package edu.ucla.tft.hilos.lightbox;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import edu.ucla.tft.hilos.BaseActivity;
import edu.ucla.tft.hilos.R;

/**
 * Class used to draw list of scrollable keywords. Provides notifications to
 * interested listeners when a particular keyword is selected.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class KeywordList extends View implements LightBoxItemContainer.Listener {
	public static final int MAX_KEYWORDS_COUNT = 20;

	public static interface Listener {
		public void onKeywordSelected(String keyword);
	}

	private float mTextSize;
	private int mPadding;
	private Bitmap mScrollLeft;
	private Bitmap mScrollLeftDisabled;
	private Bitmap mScrollRight;
	private Bitmap mScrollRightDisabled;
	private int mBgColor1;
	private int mBgColor2;
	private GradientDrawable mBgDrawable;
	private int mForeground;

	private static class KeywordGeom {
		public String word;
		public Rect rect = new Rect();
	}

	private LightBoxItemContainer mContainer;
	private Rect mScrollLeftClip = new Rect();
	private boolean mScrollLeftDown;
	private Rect mScrollRightClip = new Rect();
	private boolean mScrollRightDown;
	private Rect mTextClip = new Rect();
	private boolean mTextClipDown;
	private int mXOffset;
	private int mTextWidth;
	private KeywordGeom[] mKeywordGeoms;
	private int mKeywordsCount;

	private Listener mListener;
	private Paint mPaint;
	private String[] mKeywords;
	private Drawable mButtonSelected;
	private int mSelectedIndex = -1;

	// Location where the first touch (down) event occurs
	private int mCx, mCy;
	// Current touch location
	private int mMx, mMy;
	// Previous touch location
	private int mLastMx, mLastMy;

	private boolean mScrolling;

	public KeywordList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);

		initResources();

		mKeywordGeoms = new KeywordGeom[MAX_KEYWORDS_COUNT];
		for (int i = 0; i < mKeywordGeoms.length; i++) {
			mKeywordGeoms[i] = new KeywordGeom();
		}
	}

	public void setListener(KeywordList.Listener listener) {
		mListener = listener;
	}

	private void initResources() {
		Resources res = getResources();
		mButtonSelected = res
				.getDrawable(R.drawable.candidateview_selected_noborder);
		mTextSize = 20;
		mPadding = 8;
		mScrollLeft = BitmapFactory.decodeResource(res, R.drawable.scroll_left);
		mScrollLeftDisabled = BitmapFactory.decodeResource(res,
				R.drawable.scroll_left_disabled);
		mScrollRight = BitmapFactory.decodeResource(res,
				R.drawable.scroll_right);
		mScrollRightDisabled = BitmapFactory.decodeResource(res,
				R.drawable.scroll_right_disabled);
		mBgColor1 = Color.BLACK;
		mBgColor2 = Color.BLACK;
		mForeground = Color.WHITE;
		mBgDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, new int[] {
				mBgColor1, mBgColor2 });
		mBgDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}

	public int getPreferredHeight() {
		return (int) (mTextSize + 2 * mPadding);
	}

	public String[] getKeywords() {
		return mKeywords;
	}

	public void setKeywords(String[] keywords) {
		mXOffset = 0;
		mSelectedIndex = -1;
		if (keywords != null && keywords.length > MAX_KEYWORDS_COUNT) {
			String[] truncatedArray = new String[MAX_KEYWORDS_COUNT];
			System.arraycopy(keywords, 0, truncatedArray, 0,
					truncatedArray.length);
			keywords = truncatedArray;
		}
		mKeywords = keywords;
	}

	private boolean canScrollLeft() {
		return mXOffset < 0;
	}

	private boolean canScrollRight() {
		return mXOffset + mTextWidth + mTextClip.left > mTextClip.right;
	}

	public boolean isVisible() {
		return !(mKeywords == null || mKeywords.length < 1);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mMx = (int) event.getX();
		mMy = (int) event.getY();
		int action = event.getAction();
		boolean canScrollLeft = canScrollLeft();
		boolean canScrollRight = canScrollRight();
		if (action == MotionEvent.ACTION_DOWN) {
			mCx = mMx;
			mLastMx = mMx;
			mCy = mMy;
			mLastMy = mMy;
			mScrollLeftDown = canScrollLeft
					&& mScrollLeftClip.contains(mCx, mCy);
			mScrollRightDown = canScrollRight
					&& mScrollRightClip.contains(mCx, mCy);
			mTextClipDown = mTextClip.contains(mCx, mCy);
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (mScrolling || Math.abs(mMx - mCx) > 10) {
				mScrolling = true;
				mScrollLeftDown = canScrollLeft
						&& mScrollLeftClip.contains(mCx, mCy)
						&& mScrollLeftClip.contains(mMx, mMy);
				mScrollRightDown = canScrollRight
						&& mScrollRightClip.contains(mCx, mCy)
						&& mScrollRightClip.contains(mMx, mMy);
				int dx = mMx - mLastMx;
				if (dx > 0 && canScrollLeft) {
					scrollLeft(dx);
				} else if (dx < 0 && canScrollRight) {
					scrollRight(-dx);
				}
				mTextClipDown = false;
			}
		} else if (action == MotionEvent.ACTION_UP) {
			if (mScrollLeftDown) {
				((BaseActivity) getContext()).playEarcon(BaseActivity.EC_TICK);
				scrollLeft(50);
			} else if (mScrollRightDown) {
				((BaseActivity) getContext()).playEarcon(BaseActivity.EC_TICK);
				scrollRight(50);
			} else if (mTextClipDown) {
				((BaseActivity) getContext()).playEarcon(BaseActivity.EC_TOCK);
				selectKeywordAtLocation(mMx, mMy);
			}
			mScrolling = false;
			mScrollLeftDown = false;
			mScrollRightDown = false;
			mTextClipDown = false;
		}
		mLastMx = mMx;
		mLastMy = mMy;
		postInvalidate();
		return true;
	}

	public void scrollLeft(int amount) {
		mXOffset += amount;
		if (mXOffset > 0) {
			mXOffset = 0;
		}
		postInvalidate();
	}

	public void scrollRight(int amount) {
		mXOffset -= amount;
		if (mXOffset + mTextClip.left + mTextWidth < mTextClip.right) {
			mXOffset = mTextClip.right - mTextClip.left - mTextWidth;
			if (mXOffset > 0) {
				mXOffset = 0;
			}
		}
		postInvalidate();
	}

	public void scrollToRect(Rect rect, int gravity) {
		int newXOffset;
		switch (gravity) {
		case Gravity.LEFT:
			newXOffset = -(rect.left - mTextClip.left) + mXOffset;
			break;
		default:
		case Gravity.CENTER:
			newXOffset = -(rect.centerX() - mTextClip.centerX()) + mXOffset;
			break;
		case Gravity.RIGHT:
			newXOffset = -(rect.right - mTextClip.right) + mXOffset;
			break;
		}
		if (newXOffset < mXOffset) {
			scrollRight(mXOffset - newXOffset);
		} else {
			scrollLeft(newXOffset - mXOffset);
		}
		postInvalidate();
	}

	public void selectKeywordAtLocation(int x, int y) {
		int focusedKeywordIndex = -1;
		for (int i = 0; i < mKeywordsCount; i++) {
			if (mKeywordGeoms[i].rect.contains(x, y)) {
				focusedKeywordIndex = i;
				break;
			}
		}
		if (focusedKeywordIndex >= 0) {
			selectWordAtIndex(focusedKeywordIndex);
		}
	}

	public void selectWordAtIndex(int index) {
		mSelectedIndex = index;
		if (mListener != null) {
			mListener.onKeywordSelected(mKeywords[mSelectedIndex]);
		}
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
		selectionChanged();
	}

	@Override
	public void selectionChanged() {
		LightBoxItem item;
		if (mContainer == null
				|| (item = mContainer.getLightBoxItemAt(mContainer
						.getLightBoxItemSelected())) == null) {
			setKeywords(null);
			return;
		}
		setKeywords(item.getKeywords());
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!isVisible()) {
			mKeywordsCount = 0;
			return;
		}
		int width = getWidth();
		mKeywordsCount = mKeywords.length;
		int prefHeight = getPreferredHeight();
		mBgDrawable.setBounds(0, 0, width, prefHeight);
		mBgDrawable.draw(canvas);
		mPaint.setColor(Color.DKGRAY);

		Bitmap scrollLeft = canScrollLeft() ? mScrollLeft : mScrollLeftDisabled;
		mScrollLeftClip.set(0, 0, scrollLeft.getWidth(), prefHeight);
		canvas.save();
		canvas.clipRect(mScrollLeftClip);
		if (mScrollLeftDown) {
			mButtonSelected.setBounds(mScrollLeftClip);
			mButtonSelected.draw(canvas);
		}
		canvas.drawBitmap(
				scrollLeft,
				mScrollLeftClip.left
						+ (mScrollLeftClip.width() - scrollLeft.getWidth()) / 2,
				mScrollLeftClip.top
						+ (mScrollLeftClip.height() - scrollLeft.getHeight())
						/ 2, mPaint);
		canvas.restore();

		Bitmap scrollRight = canScrollRight() ? mScrollRight
				: mScrollRightDisabled;
		mScrollRightClip.set(width - scrollRight.getWidth(), 0, width,
				prefHeight);
		canvas.save();
		canvas.clipRect(mScrollRightClip);
		if (mScrollRightDown) {
			mButtonSelected.setBounds(mScrollRightClip);
			mButtonSelected.draw(canvas);
		}
		canvas.drawBitmap(
				scrollRight,
				mScrollRightClip.left
						+ (mScrollRightClip.width() - scrollRight.getWidth())
						/ 2,
				mScrollRightClip.top
						+ (mScrollRightClip.height() - scrollRight.getHeight())
						/ 2, mPaint);
		canvas.restore();

		mPaint.setTextSize(mTextSize);
		mPaint.setTextAlign(Align.LEFT);
		mTextClip.set(scrollLeft.getWidth() + 10, 0,
				width - scrollRight.getWidth() - 10, prefHeight);
		canvas.save();
		canvas.clipRect(mTextClip);
		int textPosX = mTextClip.left + mXOffset;
		float textPosY = prefHeight / 2 + mTextSize / 3;
		int startTextPos = textPosX;
		int txtPad = mPadding;
		textPosX += txtPad;
		Rect textBounds = new Rect();
		for (int i = 0; i < mKeywordGeoms.length && i < mKeywords.length; i++) {
			KeywordGeom wordGeom = mKeywordGeoms[i];
			wordGeom.word = mKeywords[i];
			mPaint.getTextBounds(wordGeom.word, 0, wordGeom.word.length(),
					textBounds);
			wordGeom.rect.set(textPosX - txtPad / 2, 0,
					textPosX + textBounds.width() + txtPad / 2, prefHeight);
			if (wordGeom.rect.right >= 0 && wordGeom.rect.left < width) {
				if (mTextClipDown && wordGeom.rect.contains(mMx, mMy)) {
					mButtonSelected.setBounds(wordGeom.rect);
					mButtonSelected.draw(canvas);
					mPaint.setColor(Color.BLACK);
				} else {
					mPaint.setColor(mForeground);
				}
				canvas.drawText(wordGeom.word, textPosX, textPosY, mPaint);
			}
			textPosX += textBounds.width();
			if (i < mKeywords.length - 1) {
				textPosX += txtPad;
				if (textPosX >= 0 && textPosX < width) {
					mPaint.setColor(Color.DKGRAY);
					canvas.drawLine(textPosX + 1, 5, textPosX + 1,
							prefHeight - 10, mPaint);
				}
			}
			textPosX += txtPad;
		}
		mTextWidth = textPosX - startTextPos;
		canvas.restore();

		mPaint.setColor(Color.DKGRAY);
		canvas.drawLine(0, prefHeight, width, prefHeight, mPaint);
	}
}

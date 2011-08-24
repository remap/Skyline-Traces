package edu.ucla.tft.hilos.panoromeo;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.Toast;
import edu.ucla.tft.hilos.BaseActivity;
import edu.ucla.tft.hilos.Highlight;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.Network;
import edu.ucla.tft.hilos.Resource;
import edu.ucla.tft.hilos.ResourceFilterByAttribute;
import edu.ucla.tft.hilos.StyleInfo;
import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;
import edu.ucla.tft.hilos.maplet.MapDisplay;
import edu.ucla.tft.hilos.Utils;

public class PanoDisplayView extends FrameLayout implements OnTouchListener,
		HighlightOverlayItem.OnSelectionChangedListener {
	public static interface OnHighlightSelectionChangedListener {
		public void highlightSelectionChanged(Highlight highlight,
				boolean selected);
	}

	private Drawable skyline;
	private Rect skylineRect;
	private int screenWidth;
	private int screenHeight;

	private boolean mPinned;

	private List<HighlightOverlayItem> mHighlightOverlayItems;
	private OnHighlightSelectionChangedListener mListener;

	public PanoDisplayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.TRANSPARENT);
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;

		int bmpWidth = 4800;
		int bmpHeight = 600;
		double scaleBmp = (double) screenHeight / bmpHeight;

		skylineRect = new Rect(0, 0, (int) (scaleBmp * bmpWidth),
				(int) (scaleBmp * bmpHeight));
		mHighlightOverlayItems = new LinkedList<HighlightOverlayItem>();

		setOnTouchListener(this);
		anim = new MoveAnimation();
		post(anim);
		loadHighlights();
	}

	protected void initResources() {
		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		skyline = currStyleInfo.getBitmapDrawable(StyleBitmapId.skyline);
	}

	protected void releaseResources() {
		Hilos.getCurrStyleInfo().releaseBitmapDrawable(StyleBitmapId.skyline);
	}

	public boolean isPinned() {
		return mPinned;
	}

	public void setPinned(boolean pinned) {
		this.mPinned = pinned;
	}

	@Override
	public void selectionChanged(HighlightOverlayItem item, boolean selected) {
		notifyHighlightSelectionChanged(item.highlight, selected);
		postInvalidate();
		item.highlight.setIntroRun(true);
	}

	public OnHighlightSelectionChangedListener getOnHighlightSelectionChangedListener() {
		return mListener;
	}

	public void setOnHighlightSelectionChangedListener(
			OnHighlightSelectionChangedListener l) {
		mListener = l;
	}

	protected void notifyHighlightSelectionChanged(Highlight highlight,
			boolean selected) {
		if (mListener == null) {
			return;
		}
		mListener.highlightSelectionChanged(highlight, selected);
	}

	private void loadHighlights() {
		new Thread() {
			@Override
			public void run() {
				try {
					Resource[] highlightResources = Network.getResourceList(
							new ResourceFilterByAttribute("parcel_overlay",
									"style:" + Hilos.getStyleIndex()), false);
					HighlightOverlayItem hoi;
					double scale = (double) screenHeight / 1024;
					for (Resource type_n : Utils.filterResources(
							highlightResources, new ResourceFilterByAttribute(
									"type:n"))) {
						String highlightName = type_n
								.getStringAttributeValue("parcel_overlay");
						Resource[] parcelVideos = Network.getResourceList(
								new ResourceFilterByAttribute("parcel_video"),
								false);
						Resource[] parcelThumbnails = Network.getResourceList(
								new ResourceFilterByAttribute(
										"parcel_thumbnail"), false);
						Resource type_s = Utils.findResourceByAttribute(
								highlightResources,
								new ResourceFilterByAttribute("parcel_overlay:"
										+ highlightName, "type:s"));
						Resource introVideo = Utils.findResourceByAttribute(
								parcelVideos, new ResourceFilterByAttribute(
										"parcel_video:" + highlightName));
						Resource introThumbnail = Utils
								.findResourceByAttribute(parcelThumbnails,
										new ResourceFilterByAttribute(
												"parcel_thumbnail:"
														+ highlightName));
						Resource description = Utils.findResourceByAttribute(
								parcelThumbnails,
								new ResourceFilterByAttribute(
										"parcel_description:" + highlightName));
						Highlight highlight = new Highlight(type_n, type_s,
								scale, introVideo, introThumbnail, description);
						hoi = new HighlightOverlayItem(highlight);
						hoi.setOnSelectionChangedListener(PanoDisplayView.this);
						synchronized (mHighlightOverlayItems) {
							mHighlightOverlayItems.add(hoi);
						}
					}
					postInvalidate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void calculateOffset(float value) {
		int offset = -(int) (value * skylineRect.width() / 360);
		if (Math.abs(offset - mOffset) < Math.abs(offset + skylineRect.width()
				- mOffset)) {
			newOffset = offset;
		} else {
			newOffset = offset + skylineRect.width();
		}
	}

	private int mX, mY;
	private int _mX, _mY;
	private int mOffset, newOffset;
	private MoveAnimation anim;
	private boolean mOverHighlight;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mX = (int) event.getX();
		mY = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mOverHighlight = isOverlayEvent(mX, mY)) {
				return true;
			}
			_mX = mX;
			_mY = mY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (!mOverHighlight) {
				mOffset += (mX - _mX) * 4;
			}
			break;
		case MotionEvent.ACTION_UP:
			mOverHighlight = false;
			break;
		}
		_mX = mX;
		_mY = mY;
		postInvalidate();
		return true;
	}

	private Toast mToast;

	private boolean isOverlayEvent(int x, int y) {
		for (HighlightOverlayItem item : mHighlightOverlayItems) {
			if (item.contains(x, y, mOffset, skylineRect.width())) {
				String info = item.highlight.info();
				if (info != null) {
					if (mToast != null) {
						mToast.cancel();
					}
					mToast = Toast.makeText(getContext(), info,
							Toast.LENGTH_LONG);
					mToast.show();
				}
				((BaseActivity) getContext()).playEarcon(BaseActivity.EC_TOCK);
				item.setSelected(!item.isSelected());
				return true;
			}
		}
		return false;
	}

	protected class MoveAnimation implements Runnable {
		@Override
		public void run() {
			if (!mPinned) {
				if (mOffset != newOffset) {
					int d = newOffset - mOffset;
					mOffset += Math.abs(d) > 10 ? d / 10 : d / Math.abs(d);
				}
				postInvalidate();
			}
			postDelayed(this, 10);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		skylineRect.offsetTo(mOffset, 0);
		skyline.setBounds(skylineRect);
		skyline.draw(canvas);
		int skylineWidth = skylineRect.width();
		if (mOffset > 0) {
			skylineRect.offsetTo(mOffset - skylineWidth, 0);
			skyline.setBounds(skylineRect);
			skyline.draw(canvas);
			if (mOffset > skylineWidth) {
				mOffset -= skylineWidth;
			}
		}
		if (mOffset < -(skylineWidth - screenWidth)) {
			skylineRect.offsetTo(mOffset + skylineWidth, 0);
			skyline.setBounds(skylineRect);
			skyline.draw(canvas);
			if (mOffset < -skylineWidth) {
				mOffset += skylineWidth;
			}
		}
		synchronized (mHighlightOverlayItems) {
			for (HighlightOverlayItem overlay : mHighlightOverlayItems) {
				overlay.draw(canvas, mOffset, screenWidth, skylineWidth);
			}
		}
	}
}
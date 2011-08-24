package edu.ucla.tft.hilos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;

/**
 * Abstract view for application views. Draws a background image depending on
 * the current style.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public abstract class BaseView extends FrameLayout {
	public static final int BASE_WIDTH = 1024;
	public static final int BASE_HEIGHT = 600;

	protected Rect mViewRect = new Rect();

	private Drawable mBackground;
	private double mScaleView;

	public BaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initResources();
		setFocusable(true);

		setBackgroundColor(Color.TRANSPARENT);
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		mScaleView = (double) height / BASE_HEIGHT;
		int viewWidth = (int) (mScaleView * BASE_WIDTH);
		int viewHeight = (int) (mScaleView * BASE_HEIGHT);
		mViewRect.left = (width - viewWidth) / 2;
		mViewRect.right = mViewRect.left + viewWidth;
		mViewRect.top = (height - viewHeight) / 2;
		mViewRect.bottom = mViewRect.top + viewHeight;
	}

	protected void initResources() {
		mBackground = Hilos.getCurrStyleInfo().getBitmapDrawable(
				StyleBitmapId.bg_base);
	}

	protected void releaseResources() {
		Hilos.getCurrStyleInfo().releaseBitmapDrawable(StyleBitmapId.bg_base);
	}

	public void draw(Canvas canvas) {
		canvas.clipRect(mViewRect);
		mBackground.setBounds(mViewRect);
		mBackground.draw(canvas);
		super.draw(canvas);
	}
}
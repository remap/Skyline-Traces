package edu.ucla.tft.hilos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;

/**
 * View used by the Splash activity.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class SplashView extends BaseView {
	private BitmapDrawable mHilosLogoDrawable;
	private BitmapDrawable mHilosLogoPressedDrawable;
	private Rect mHilosLogoRect;

	private boolean pressed;

	public SplashView(Context context, AttributeSet attrs) {
		super(context, attrs);
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		StyleInfo styleInfo = Hilos.getCurrStyleInfo();
		mHilosLogoDrawable = styleInfo.getBitmapDrawable(StyleBitmapId.skyline_traces_logo);
		mHilosLogoPressedDrawable = styleInfo
				.getBitmapDrawable(StyleBitmapId.skyline_traces_logo_pressed);
		// Resources res = getResources();
		// mHilosLogoDrawable = (BitmapDrawable) res
		// .getDrawable(R.drawable.skyline_traces_logo);
		// mHilosLogoPressedDrawable = (BitmapDrawable) res
		// .getDrawable(R.drawable.skyline_traces_logo_pressed);
		Bitmap bitmap;
		int bmp_width, bmp_height;
		double scale;
		int logo_width, logo_height;
		bitmap = mHilosLogoDrawable.getBitmap();
		bmp_width = bitmap.getWidth();
		bmp_height = bitmap.getHeight();
		scale = 0.8;
		logo_width = (int) (bmp_width * scale);
		logo_height = (int) (bmp_height * scale);
		mHilosLogoRect = new Rect();
		mHilosLogoRect.left = (width - logo_width) / 2;
		mHilosLogoRect.top = (height - logo_height) / 2;
		mHilosLogoRect.right = mHilosLogoRect.left + logo_width;
		mHilosLogoRect.bottom = mHilosLogoRect.top + logo_height;
	}

	public boolean doContinue(int x, int y, int action) {
		boolean doContinue = false;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (mHilosLogoRect.contains(x, y)) {
				pressed = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (pressed) {
				pressed = false;
				doContinue = mHilosLogoRect.contains(x, y);
			}
		}
		postInvalidate();
		return doContinue;
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);
		Drawable d = pressed ? mHilosLogoPressedDrawable : mHilosLogoDrawable;
		d.setBounds(mHilosLogoRect);
		d.draw(canvas);
	}
}
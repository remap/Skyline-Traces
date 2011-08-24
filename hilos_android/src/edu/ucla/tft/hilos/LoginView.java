package edu.ucla.tft.hilos;

import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

/**
 * View used by the Login activity. Shows fields to input email and password.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class LoginView extends BaseView {
	private BitmapDrawable mHilosLogoDrawable;
	private Rect mHilosLogoRect;

	public LoginView(Context context, AttributeSet attrs) {
		super(context, attrs);
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		StyleInfo styleInfo = Hilos.getCurrStyleInfo();
		mHilosLogoDrawable = styleInfo
				.getBitmapDrawable(StyleBitmapId.skyline_traces_logo);
		Bitmap bitmap;
		int bmp_width, bmp_height;
		double scale;
		int logo_width, logo_height;
		bitmap = mHilosLogoDrawable.getBitmap();
		bmp_width = bitmap.getWidth();
		bmp_height = bitmap.getHeight();
		scale = 0.4;
		logo_width = (int) (bmp_width * scale);
		logo_height = (int) (bmp_height * scale);
		mHilosLogoRect = new Rect();
		mHilosLogoRect.left = (width - logo_width) / 2;
		mHilosLogoRect.top = (height / 4 - logo_height) / 2;
		mHilosLogoRect.right = mHilosLogoRect.left + logo_width;
		mHilosLogoRect.bottom = mHilosLogoRect.top + logo_height;
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);
		mHilosLogoDrawable.setBounds(mHilosLogoRect);
		mHilosLogoDrawable.draw(canvas);
	}
}
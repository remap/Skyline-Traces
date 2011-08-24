package edu.ucla.tft.hilos.panoromeo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import edu.ucla.tft.hilos.Highlight;
import edu.ucla.tft.hilos.Hilos;

/**
 * Represents an overlay for highlights (also referred to as 'parcels') on the
 * skyline display. Provides functions to change current state (none,
 * highlighted or selected), draw the overlay image depending on state and
 * notifying listeners when the state changes.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class HighlightOverlayItem {
	public static interface OnSelectionChangedListener {
		public void selectionChanged(HighlightOverlayItem item, boolean selected);
	}

	private static HighlightOverlayItem sSelectedItem;
	private static Paint sDefPaint;
	private static Paint sTextPaint;
	private static Paint sTextSelectedPaint;
	static {
		sDefPaint = new Paint();
		sDefPaint.setColor(Color.WHITE);
		sTextPaint = new Paint();
		sTextPaint.setAntiAlias(true);
		sTextPaint.setColor(Color.WHITE);
		sTextPaint.setTextSize(16.0f);
		sTextPaint.setTextAlign(Align.CENTER);
		sTextSelectedPaint = new Paint();
		sTextSelectedPaint.setAntiAlias(true);
		sTextSelectedPaint.setColor(Color.BLACK);
		sTextSelectedPaint.setTextSize(16.0f);
		sTextSelectedPaint.setTextAlign(Align.CENTER);
	}

	public final Highlight highlight;
	private Rect mBounds;
	private HighlightOverlayItem.OnSelectionChangedListener mListener;

	public HighlightOverlayItem(Highlight highlight) {
		this.highlight = highlight;
		mBounds = new Rect();
	}

	public HighlightOverlayItem.OnSelectionChangedListener getOnSelectionChangedListener() {
		return mListener;
	}

	public void setOnSelectionChangedListener(
			HighlightOverlayItem.OnSelectionChangedListener l) {
		mListener = l;
	}

	protected void notifySelectionChanged(boolean selected) {
		if (mListener == null) {
			return;
		}
		mListener.selectionChanged(this, selected);
	}

	public boolean contains(int x, int y, int xOffset, int skylineWidth) {
		return mBounds.contains(x, y)
				|| isWithinBitmap(x, y, xOffset, skylineWidth);
	}

	public boolean isSelected() {
		return sSelectedItem == this;
	}

	public void setSelected(boolean b) {
		if (b) {
			if (sSelectedItem != null) {
				sSelectedItem.notifySelectionChanged(false);
				sSelectedItem = null;
			}
			if (!isSelected()) {
				sSelectedItem = this;
				sSelectedItem.notifySelectionChanged(true);
			}
		} else {
			if (isSelected()) {
				sSelectedItem.notifySelectionChanged(false);
				sSelectedItem = null;
			}
		}
	}

	public boolean isWithinBitmap(int x, int y, int xOffset, int skylineWidth) {
		Bitmap bitmap = highlight.bitmap(false);
		int refpx_bmpcs = x - calc_x(xOffset, skylineWidth);
		int refpy_bmpcs = y - highlight.positionY(false);
		if (refpx_bmpcs < 0 || refpx_bmpcs >= bitmap.getWidth()
				|| refpy_bmpcs < 0 || refpy_bmpcs >= bitmap.getHeight()) {
			return false;
		}
		return (bitmap.getPixel(refpx_bmpcs, refpy_bmpcs) & 0xff000000) != 0;
	}

	public void draw(Canvas canvas, int xOffset, int screenWidth,
			int skylineWidth) {
		int x = calc_x(xOffset, skylineWidth);
		boolean selected = isSelected();
		Bitmap bitmap = highlight.bitmap(selected);
		double col = (x + bitmap.getWidth() / 2) * 4.0 / screenWidth;
		int alpha = 0;
		switch ((int) Math.floor(col)) {
		case 0:
			alpha = (int) (col * col * 0x100);
			break;
		case 1:
		case 2:
			alpha = 0xff;
			break;
		case 3:
			alpha = (int) ((4 - col) * (4 - col) * 0x100);
			break;
		}
		if (alpha > 0) {
			draw(canvas, alpha, bitmap, x, highlight.positionY(selected),
					highlight.name(selected));
		}
	}

	private int calc_x(int xOffset, int skylineWidth) {
		int x = highlight.positionX(isSelected()) + xOffset;
		if (x < 0) {
			x += skylineWidth;
		}
		x %= skylineWidth;
		return x;
	}

	private void draw(Canvas canvas, int alpha, Bitmap bitmap, int x, int y,
			String text) {
		sDefPaint.setAlpha(alpha);
		canvas.drawBitmap(bitmap, x, y, sDefPaint);
		sTextPaint.getTextBounds(text, 0, text.length(), mBounds);
		int tx = x + bitmap.getWidth() / 2;
		int ty = y - 20;
		mBounds.offsetTo(tx - mBounds.width() / 2, ty - mBounds.height());
		mBounds.left -= 15;
		mBounds.top -= 15;
		mBounds.right += 15;
		mBounds.bottom += 15;
		Drawable d = isSelected() ? Hilos.sBackgroundTextSelectedDrawable
				: Hilos.sBackgroundTextDrawable;
		d.setBounds(mBounds);
		d.setAlpha(isSelected() ? 0xff : alpha / 2);
		d.draw(canvas);
		sTextPaint.setAlpha(isSelected() ? 0xff : alpha);
		canvas.drawText(text, tx, ty, isSelected() ? sTextSelectedPaint
				: sTextPaint);
	}
}
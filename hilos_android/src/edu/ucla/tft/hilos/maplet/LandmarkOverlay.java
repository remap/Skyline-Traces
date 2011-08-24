package edu.ucla.tft.hilos.maplet;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.Landmark;

/**
 * Responsible for drawing a {@link Landmark} on the map.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class LandmarkOverlay extends Overlay {
	public static interface OnSelectionChangedListener {
		public void selectionChanged(LandmarkOverlay landmarkOverlay,
				boolean selected);
	}

	private static LandmarkOverlay sSelected;

	private static Paint sTextPaint;
	private static Paint sTextSelectedPaint;
	static {
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
	private static OnSelectionChangedListener sListener;

	public final Landmark landmark;
	private MapDisplay mMapDisplay;
	private Point bmpScreenCoords = new Point();
	private Point refScreenCoords = new Point();
	private Paint mPaint = new Paint();
	private Rect mBounds;

	private boolean mShowing;
	private boolean mHighlighted;

	public LandmarkOverlay(MapDisplay mapDisplay, Landmark landmark) {
		super(mapDisplay);
		this.landmark = landmark;
		mMapDisplay = mapDisplay;
		mBounds = new Rect();
	}

	public static OnSelectionChangedListener getOnSelectionChangedListener() {
		return sListener;
	}

	public static void setOnSelectionChangedListener(
			OnSelectionChangedListener l) {
		sListener = l;
	}

	protected static void notifySelectionChanged(
			LandmarkOverlay landmarkOverlay, boolean selected) {
		if (selected) {
			sSelected = landmarkOverlay;
		} else if (!selected && sSelected == landmarkOverlay) {
			sSelected = null;
		}
		if (sListener == null) {
			return;
		}
		sListener.selectionChanged(landmarkOverlay, selected);
	}

	public static LandmarkOverlay getSelected() {
		return sSelected;
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}
		boolean selected = LandmarkOverlay.sSelected == this;
		Bitmap bitmap = landmark.bitmap(selected);
		if (bitmap == null) {
			return;
		}
		GeoPoint refPoint = mMapDisplay.isPinned() ? mapView.getMapCenter()
				: mMapDisplay.getVirtualLocation();
		Projection pj = mapView.getProjection();
		pj.toMapPixels(refPoint, refScreenCoords);
		pj.toMapPixels(landmark.location(selected), bmpScreenCoords);
		int zoom_diff = landmark.zoomLevel(selected)
				- mapView.getZoomLevel(false);
		float s = zoom_diff >= 0 ? 1.0f / (0x1 << zoom_diff)
				: (0x1 << -zoom_diff);
		float width = bitmap.getWidth() * s;
		float height = bitmap.getHeight() * s;
		float x = bmpScreenCoords.x - width / 2;
		float y = bmpScreenCoords.y - height / 2;
		int refpx_bmpcs = (int) ((refScreenCoords.x - bmpScreenCoords.x) / s + bitmap
				.getWidth() / 2);
		int refpy_bmpcs = (int) ((refScreenCoords.y - bmpScreenCoords.y) / s + bitmap
				.getHeight() / 2);
		boolean show = !Hilos.isTrackLocation()
				|| isWithinBitmapSmooth(bitmap, refpx_bmpcs, refpy_bmpcs);
		if (show && !mShowing) {
			notifySelectionChanged(this, true);
		} else if (!show && mShowing) {
			notifySelectionChanged(this, false);
		}
		mShowing = show;
		if (!mShowing && !mHighlighted) {
			return;
		}
		canvas.save();
		canvas.scale(s, s, x, y);
		canvas.drawBitmap(bitmap, x, y, this.mPaint);
		canvas.restore();
		String text = landmark.name(selected);
		sTextPaint.getTextBounds(text, 0, text.length(), mBounds);
		int tx = (int) (x + bitmap.getWidth() * s / 2);
		int ty = (int) (y + bitmap.getHeight() * s / 2);
		mBounds.offsetTo(tx - mBounds.width() / 2, ty - mBounds.height());
		mBounds.left -= 15;
		mBounds.top -= 15;
		mBounds.right += 15;
		mBounds.bottom += 15;
		Drawable d = selected ? Hilos.sBackgroundTextSelectedDrawable
				: Hilos.sBackgroundTextDrawable;
		d.setBounds(mBounds);
		int alpha = 0xff;
		d.setAlpha(selected ? 0xff : alpha / 2);
		d.draw(canvas);
		sTextPaint.setAlpha(selected ? 0xff : alpha);
		canvas.drawText(text, tx, ty, selected ? sTextSelectedPaint
				: sTextPaint);
	}

	public boolean isHighlighted() {
		return mHighlighted;
	}

	public void setHighlighted(boolean highlighted) {
		this.mHighlighted = highlighted;
	}

	private Bitmap mLastBitmap;
	private int mLastRefpx_bmpcs, mLastRefpy_bmpcs;
	private boolean mLastWithinBitmap;

	private boolean isWithinBitmapSmooth(Bitmap bitmap, int refpx_bmpcs,
			int refpy_bmpcs) {
		if (bitmap == mLastBitmap && mLastRefpx_bmpcs == refpx_bmpcs
				&& mLastRefpy_bmpcs == refpy_bmpcs) {
			return mLastWithinBitmap;
		}
		boolean withinBitmap = isWithinBitmap(bitmap, refpx_bmpcs, refpy_bmpcs);
		if (!withinBitmap
				&& mLastWithinBitmap
				&& (isWithinBitmap(bitmap, refpx_bmpcs - 20, refpy_bmpcs - 20)
						|| isWithinBitmap(bitmap, refpx_bmpcs + 20,
								refpy_bmpcs - 20)
						|| isWithinBitmap(bitmap, refpx_bmpcs - 20,
								refpy_bmpcs + 20) || isWithinBitmap(bitmap,
						refpx_bmpcs + 20, refpy_bmpcs + 20))) {
			withinBitmap = true;
		}
		mLastRefpx_bmpcs = refpx_bmpcs;
		mLastRefpy_bmpcs = refpy_bmpcs;
		mLastWithinBitmap = withinBitmap;
		mLastBitmap = bitmap;
		return withinBitmap;
	}

	private boolean isWithinBitmap(Bitmap bitmap, int refpx_bmpcs,
			int refpy_bmpcs) {
		if (refpx_bmpcs < 0 || refpx_bmpcs >= bitmap.getWidth()
				|| refpy_bmpcs < 0 || refpy_bmpcs >= bitmap.getHeight()) {
			return false;
		}
		return (bitmap.getPixel(refpx_bmpcs, refpy_bmpcs) & 0xff000000) != 0;
	}

	public boolean isWithinBitmap(MapView mapView, GeoPoint point) {
		boolean selected = LandmarkOverlay.sSelected == this;
		Bitmap bitmap = landmark.bitmap(selected);
		if (bitmap == null) {
			return false;
		}
		Projection pj = mapView.getProjection();
		pj.toMapPixels(point, refScreenCoords);
		pj.toMapPixels(landmark.location(selected), bmpScreenCoords);
		int zoom_diff = landmark.zoomLevel(selected)
				- mapView.getZoomLevel(false);
		float s = zoom_diff >= 0 ? 1.0f / (0x1 << zoom_diff)
				: (0x1 << -zoom_diff);
		int refpx_bmpcs = (int) ((refScreenCoords.x - bmpScreenCoords.x) / s + bitmap
				.getWidth() / 2);
		int refpy_bmpcs = (int) ((refScreenCoords.y - bmpScreenCoords.y) / s + bitmap
				.getHeight() / 2);
		return isWithinBitmap(bitmap, refpx_bmpcs, refpy_bmpcs);
	}
}
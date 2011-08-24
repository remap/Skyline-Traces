package edu.ucla.tft.hilos.citylens;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import edu.ucla.tft.hilos.AbstractToolbar;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.StyleInfo;
import edu.ucla.tft.hilos.StyleInfo.StyleNinePatchId;

/**
 * Toolbar providing zoom controls etc.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class CityLensZoomToolbar extends AbstractToolbar {
	public static final int HOME = 0, ZOOMIN = 1, ZOOMOUT = 2;
	private static final int[] sButton2Id;
	static {
		sButton2Id = new int[3];
		sButton2Id[HOME] = R.id.citylens_home;
		sButton2Id[ZOOMIN] = R.id.citylens_zoomin;
		sButton2Id[ZOOMOUT] = R.id.citylens_zoomout;
	}

	public CityLensZoomToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		View bg_toolbar = findViewById(R.id.bg_zoom_toolbar);
		bg_toolbar.setBackgroundDrawable(currStyleInfo
				.getNinePatchDrawable(StyleNinePatchId.bg_toolbar));
		bg_toolbar.setPadding(40, 0, 40, 0);
		Button button;
		button = (Button) findViewById(R.id.citylens_home);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_home));
		button = (Button) findViewById(R.id.citylens_zoomin);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_zoomin));
		button = (Button) findViewById(R.id.citylens_zoomout);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_zoomout));
	}

	@Override
	protected int getToolbarResId() {
		return R.layout.citylens_zoom_toolbar;
	}

	@Override
	protected int[] getButtonToIdMap() {
		return sButton2Id;
	}
}

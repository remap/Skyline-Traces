package edu.ucla.tft.hilos.maplet;

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
 * A toolbar for the {@link MapDisplay} activity.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class MapToolbar extends AbstractToolbar {
	public static final int HOME = 0, INFO = 1, HELP = 2, PIN = 3, ZOOMIN = 4,
			ZOOMOUT = 5;
	private static final int[] sButton2Id;
	static {
		sButton2Id = new int[6];
		sButton2Id[HOME] = R.id.map_home;
		sButton2Id[INFO] = R.id.map_info;
		sButton2Id[HELP] = R.id.map_help;
		sButton2Id[PIN] = R.id.map_pin;
		sButton2Id[ZOOMIN] = R.id.map_zoomin;
		sButton2Id[ZOOMOUT] = R.id.map_zoomout;
	}

	public MapToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		View bg_toolbar = findViewById(R.id.bg_map_toolbar);
		bg_toolbar.setBackgroundDrawable(currStyleInfo
				.getNinePatchDrawable(StyleNinePatchId.bg_toolbar));
		bg_toolbar.setPadding(40, 0, 40, 0);
		Button button;
		button = (Button) findViewById(R.id.map_home);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_home));
		button = (Button) findViewById(R.id.map_info);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_info));
		button = (Button) findViewById(R.id.map_help);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_help));
		button = (Button) findViewById(R.id.map_pin);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_pin));
		button = (Button) findViewById(R.id.map_zoomin);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_zoomin));
		button = (Button) findViewById(R.id.map_zoomout);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_zoomout));
	}

	@Override
	protected int getToolbarResId() {
		return R.layout.map_toolbar;
	}

	@Override
	protected int[] getButtonToIdMap() {
		return sButton2Id;
	}
}

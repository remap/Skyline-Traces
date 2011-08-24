package edu.ucla.tft.hilos.panoromeo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import edu.ucla.tft.hilos.AbstractToolbar;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.StyleInfo;
import edu.ucla.tft.hilos.StyleInfo.StyleNinePatchId;

public class PanoToolbar extends AbstractToolbar {
	public static final int HOME = 0, INFO = 1, HELP = 2, PIN = 3,
			CITYLENS = 4;
	private static final int[] sButton2Id;
	static {
		sButton2Id = new int[5];
		sButton2Id[HOME] = R.id.pano_home;
		sButton2Id[INFO] = R.id.pano_info;
		sButton2Id[HELP] = R.id.pano_help;
		sButton2Id[PIN] = R.id.pano_pin;
		sButton2Id[CITYLENS] = R.id.pano_citylens;
	}

	public PanoToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		View bg_toolbar = findViewById(R.id.bg_pano_toolbar);
		bg_toolbar.setBackgroundDrawable(currStyleInfo
				.getNinePatchDrawable(StyleNinePatchId.bg_toolbar));
		bg_toolbar.setPadding(40, 0, 40, 0);
		Button button;
		button = (Button) findViewById(R.id.pano_home);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_home));
		button = (Button) findViewById(R.id.pano_info);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_info));
		button = (Button) findViewById(R.id.pano_help);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_help));
		button = (Button) findViewById(R.id.pano_pin);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_pin));
		button = (Button) findViewById(R.id.pano_citylens);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_citylens));
	}

	@Override
	protected int getToolbarResId() {
		return R.layout.pano_toolbar;
	}

	@Override
	protected int[] getButtonToIdMap() {
		return sButton2Id;
	}
}

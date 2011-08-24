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
 * Toolbar providing rotation controls.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class CityLensRotateToolbar extends AbstractToolbar {
	public static final int ROTATE_LEFT = 0, ROTATE_RIGHT = 1;
	private static final int[] sButton2Id;
	static {
		sButton2Id = new int[2];
		sButton2Id[ROTATE_LEFT] = R.id.citylens_rotate_left;
		sButton2Id[ROTATE_RIGHT] = R.id.citylens_rotate_right;
	}

	public CityLensRotateToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		View bg_toolbar = findViewById(R.id.bg_rotate_toolbar);
		bg_toolbar.setBackgroundDrawable(currStyleInfo
				.getNinePatchDrawable(StyleNinePatchId.bg_toolbar));
		bg_toolbar.setPadding(40, 0, 40, 0);
		Button button;
		button = (Button) findViewById(R.id.citylens_rotate_left);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_rotate_left));
		button = (Button) findViewById(R.id.citylens_rotate_right);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.toolbar_rotate_right));
	}

	@Override
	protected int getToolbarResId() {
		return R.layout.citylens_rotate_toolbar;
	}

	@Override
	protected int[] getButtonToIdMap() {
		return sButton2Id;
	}
}

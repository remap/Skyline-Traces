package edu.ucla.tft.hilos.lightbox;

import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.StyleInfo;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * A view for {@link LightBoxActivity}.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class LightBoxView extends FrameLayout {
	protected Rect mViewRect = new Rect();

	public LightBoxView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);
		setBackgroundColor(Color.BLACK);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		Button button;
		button = (Button) findViewById(R.id.prev);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.btn_prev));
		button = (Button) findViewById(R.id.next);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.btn_next));
		button = (Button) findViewById(R.id.playPause);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.btn_play));
		button = (Button) findViewById(R.id.volume);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.btn_volume_hi));
		button = (Button) findViewById(R.id.minimize);
		button.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleInfo.StyleStateListId.btn_minimize));
	}
}
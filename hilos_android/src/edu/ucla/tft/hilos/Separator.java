package edu.ucla.tft.hilos;

import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A view which shows a <i>separator</i> image, to be used in {@link AbstractToolbar}s.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class Separator extends ImageView {
	public Separator(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		setImageDrawable(Hilos.getCurrStyleInfo().getBitmapDrawable(
				StyleBitmapId.toolbar_horizontal_separator));
	}
}

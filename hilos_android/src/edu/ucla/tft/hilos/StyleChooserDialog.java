package edu.ucla.tft.hilos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;

/**
 * To create a dialog box to choose the current style.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class StyleChooserDialog {
	private static int currIndex = 0;

	public static Dialog create(Context context,
			DialogInterface.OnClickListener onSelect) {
		Resources res = context.getResources();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(res.getString(R.string.style));
		builder.setPositiveButton("Select", onSelect);
		View view = LayoutInflater.from(context).inflate(
				R.layout.style_chooser_dialog, null);
		final String[] styleNames = new String[Hilos.getStylesCount()];
		final Bitmap[] styleIcons = new Bitmap[Hilos.getStylesCount()];
		for (int i = 0; i < styleIcons.length; i++) {
			StyleInfo styleInfo = Hilos.getStyleInfo(i);
			styleNames[i] = styleInfo.name;
			styleIcons[i] = styleInfo.getBitmap(StyleBitmapId.ic_style);
		}
		final ImageView styleIconView = (ImageView) view
				.findViewById(R.id.styleIconView);
		styleIconView.setImageBitmap(styleIcons[currIndex]);
		final TextView styleName = (TextView) view
				.findViewById(R.id.style_title);
		styleName.setText(styleNames[currIndex]);
		OnClickListener nav = new OnClickListener() {
			@Override
			public void onClick(View v) {
				currIndex = (currIndex + (Integer) v.getTag() + styleIcons.length)
						% styleIcons.length;
				styleName.setText(styleNames[currIndex]);
				styleIconView.setImageBitmap(styleIcons[currIndex]);
			}
		};
		Button prev = (Button) view.findViewById(R.id.style_prev);
		prev.setTag(-1);
		prev.setOnClickListener(nav);
		Button next = (Button) view.findViewById(R.id.style_next);
		next.setTag(1);
		next.setOnClickListener(nav);
		builder.setView(view);
		builder.setCancelable(true);
		return builder.create();
	}

	public static int getCurrIndex() {
		return currIndex;
	}
}

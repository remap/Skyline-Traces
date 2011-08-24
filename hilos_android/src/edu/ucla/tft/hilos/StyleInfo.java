package edu.ucla.tft.hilos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Environment;

/**
 * Maintains a list of application assets for a particular style.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class StyleInfo {
	private static File sStylesDir = new File(
			Environment.getExternalStorageDirectory() + "/hilos/styles");
	static {
		if (!sStylesDir.exists()) {
			sStylesDir.mkdirs();
		}
	}
	private static StyleInfo defaultStyle;

	public final int index;
	public final String name;
	public final int systemTheme;
	public final int textForegroundColor;
	public final int textShadowColor;
	public final File styleDir;

	public StyleInfo(int index, String name, int systemTheme,
			int textForegroundColor, int textShadowColor, boolean isDefault) {
		this.index = index;
		this.name = name;
		this.systemTheme = systemTheme;
		this.textForegroundColor = textForegroundColor;
		this.textShadowColor = textShadowColor;
		this.styleDir = new File(sStylesDir + "/" + name);
		if (!this.styleDir.exists()) {
			this.styleDir.mkdir();
		}
		if (defaultStyle == null || isDefault) {
			defaultStyle = this;
		}
	}

	public static StyleInfo getDefault() {
		return defaultStyle;
	}

	public boolean isAvailable() {
		String[] files;
		return styleDir.exists() && (files = styleDir.list()) != null
				&& files.length > 0;
	}

	public boolean load() {
		File tmpStyleDir = new File(sStylesDir + "/temp");
		if (!tmpStyleDir.exists()) {
			tmpStyleDir.mkdirs();
		}
		try {
			String[] styleItems = Network.getStyleItemList(index);
			if (styleItems == null) {
				return false;
			}
			for (String styleItem : styleItems) {
				InputStream in;
				FileOutputStream out;
				in = Network.getStyleItemInputStream(index, styleItem);
				if (in == null) {
					return false;
				}
				out = new FileOutputStream(tmpStyleDir + "/" + styleItem);
				Utils.readFully(in, out);
				out.close();
				in.close();
			}
			tmpStyleDir.renameTo(this.styleDir);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tmpStyleDir.exists()) {
				Utils.delete(tmpStyleDir);
			}
		}
		return false;
	}

	public static enum StyleStateListId {
		btn_fullscreen, btn_minimize, btn_next, btn_pause, btn_play, btn_prev, btn_settings, btn_volume_hi, btn_volume_lo, btn_volume_mute, toolbar_home, //
		toolbar_info, //
		toolbar_help, //
		toolbar_apply, //
		toolbar_play, //
		toolbar_galerie, //
		toolbar_zoomin, //
		toolbar_zoomout, //
		toolbar_pin, //
		toolbar_citylens, //
		toolbar_rotate_left, //
		toolbar_rotate_right, //
	}

	public static enum StyleBitmapId {
		ic_error, //
		ic_image, //
		ic_loading, //
		ic_media_ff, //
		ic_media_pause, //
		ic_media_play, //
		ic_media_rew, //
		ic_overlay_audio, //
		ic_overlay_audio_selected, //
		ic_overlay_image, //
		ic_overlay_image_selected, //
		ic_overlay_video, //
		ic_overlay_video_selected, //
		ic_thumb_audio, //
		ic_thumb_image, //
		ic_thumb_video, //
		ic_volume, //
		ic_style, //
		toolbar_horizontal_separator, //
		bg_base, //
		skyline, //
		skyline_traces_logo, //
		skyline_traces_logo_pressed, //
		skyline_traces_logo_sm
	}

	public static enum StyleNinePatchId {
		bg_text, //
		bg_text_selected, //
		bg_toolbar, //
	}

	public static enum SoundId {
		deselect_snd, //
		finish_snd, //
		flip_snd, //
		hit_snd, //
		map, //
		pinning_snd, //
		pop_snd, //
		select_snd, //
		splash, //
		start_snd, //
		tick_snd, //
		tock_snd, //
	}

	private static final String[] sSupportedBitmapExtensions = { "png",
			"9.png", "jpg" };

	private File getBitmapFile(String id_name) {
		File file;
		for (int i = 0; i < sSupportedBitmapExtensions.length; i++) {
			if ((file = new File(styleDir + "/" + id_name + "."
					+ sSupportedBitmapExtensions[i])).exists()) {
				return file;
			}
		}
		if (this != defaultStyle) {
			// check default style
			for (int i = 0; i < sSupportedBitmapExtensions.length; i++) {
				if ((file = new File(defaultStyle.styleDir + "/" + id_name
						+ "." + sSupportedBitmapExtensions[i])).exists()) {
					return file;
				}
			}
		}
		return null;
	}

	private Bitmap getBitmap(String id_name) {
		return getBitmap(id_name, null);
	}

	private Map<String, Bitmap> mBitmapsCache = new HashMap<String, Bitmap>();

	private Bitmap getBitmap(String id_name, Rect rect) {
		Bitmap bitmap;
		if ((bitmap = mBitmapsCache.get(id_name)) != null) {
			return bitmap;
		}
		File file = getBitmapFile(id_name);
		if (file == null) {
			return null;
		}
		if (rect == null) {
			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		} else {
			try {
				InputStream is = new FileInputStream(file);
				bitmap = BitmapFactory.decodeStream(is, rect, null);
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mBitmapsCache.put(id_name, bitmap);
		return bitmap;
	}

	public synchronized Bitmap getBitmap(StyleBitmapId id) {
		return getBitmap(id.name());
	}

	public synchronized BitmapDrawable getBitmapDrawable(StyleBitmapId id) {
		return new BitmapDrawable(getBitmap(id.name()));
	}

	public synchronized void releaseBitmapDrawable(StyleBitmapId id) {
		Bitmap bitmap = mBitmapsCache.remove(id.name());
		if (bitmap != null) {
			bitmap.recycle();
		}
	}

	public synchronized NinePatchDrawable getNinePatchDrawable(StyleNinePatchId id) {
		File file = getBitmapFile(id.name());
		if (file == null) {
			return null;
		}
		NinePatchDrawable d = null;
		Rect rect = new Rect();
		Bitmap bitmap = getBitmap(id.name(), rect);
		d = new NinePatchDrawable(bitmap, bitmap.getNinePatchChunk(), rect,
				null);
		return d;
	}

	public synchronized void releaseNinePatchDrawable(StyleBitmapId id) {
		Bitmap bitmap = mBitmapsCache.remove(id.name());
		if (bitmap != null) {
			bitmap.recycle();
		}
	}

	public synchronized StateListDrawable getStateListDrawable(StyleStateListId id) {
		StateListDrawable sld = new StateListDrawable();
		Bitmap bitmap;
		if ((bitmap = getBitmap("ic_" + id.name() + "_disabled")) != null) {
			sld.addState(new int[] { -android.R.attr.state_enabled },
					new BitmapDrawable(bitmap));
		}
		if ((bitmap = getBitmap("ic_" + id.name() + "_selected")) != null) {
			sld.addState(new int[] { android.R.attr.state_selected },
					new BitmapDrawable(getBitmap("ic_" + id.name()
							+ "_selected")));
			sld.addState(new int[] { android.R.attr.state_pressed },
					new BitmapDrawable(getBitmap("ic_" + id.name()
							+ "_selected")));
		}
		sld.addState(new int[] { -android.R.attr.state_pressed,
				-android.R.attr.state_selected }, new BitmapDrawable(
				getBitmap("ic_" + id.name())));
		return sld;
	}

	private static final String[] sSupportedSoundExtensions = { "ogg", "mp3" };

	private File getSoundFile(String id_name) {
		File file;
		for (int i = 0; i < sSupportedSoundExtensions.length; i++) {
			if ((file = new File(styleDir + "/" + id_name + "."
					+ sSupportedSoundExtensions[i])).exists()) {
				return file;
			}
			System.err.println("=== file: " + file);
		}
		if (this != defaultStyle) {
			// check default style
			for (int i = 0; i < sSupportedSoundExtensions.length; i++) {
				if ((file = new File(defaultStyle.styleDir + "/" + id_name
						+ "." + sSupportedSoundExtensions[i])).exists()) {
					return file;
				}
			}
		}
		return null;
	}

	public String getSoundFilePath(SoundId id) {
		File soundFile = getSoundFile(id.toString());
		return soundFile == null ? null : soundFile.getAbsolutePath();
	}
}
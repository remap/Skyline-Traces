package edu.ucla.tft.hilos.lightbox;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.Keywords;
import edu.ucla.tft.hilos.Resource;
import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;
import edu.ucla.tft.hilos.Utils;

/**
 * An interface for an item that can be displayed in {@link LightBoxActivity} or
 * {@link LightBoxMini}.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public interface LightBoxItem extends Serializable {
	public static class Caption {
		public static final Caption NULL = new Caption("");
		public final String text;
		public final long showAt;
		public final long duration;

		public Caption(String text) {
			this(text, 0, 0);
		}

		public Caption(String text, long showAt, long duration) {
			this.text = text;
			this.showAt = showAt;
			this.duration = duration;
		}
	}

	public String getTitle();

	public String getDate();

	public Caption[] getCaptions();

	public String[] getKeywords();

	public Object getData();

	public void releaseData();

	public Bitmap getThumbnail(Context context);

	public boolean isImageType();

	public boolean isAudioType();

	public boolean isVideoType();

	/**
	 * A <code>LightBoxItem</code> backed by an online {@link Resource}.
	 * 
	 * @author Jasleen Singh (jasleen@cs.ucla.edu)
	 * 
	 */
	public static class ResourceLightBoxItem implements LightBoxItem {
		private static Bitmap sImageIcon;
		private static Bitmap sAudioIcon;
		private static Bitmap sVideoIcon;
		private final Resource resource;
		protected Caption[] captions;
		protected String[] keywords;
		protected Object data;
		protected Bitmap thumbnail;

		public ResourceLightBoxItem(Resource r, Object data) {
			this(r, data, null);
		}

		public ResourceLightBoxItem(Resource r, Object data, Bitmap thumbnail) {
			this.resource = r;
			this.captions = new Caption[] { new Caption(r.description) };
			this.keywords = Utils.getTopKeywordNames(
					new Keywords[] { r.keywords }, 0);
			this.data = data;
			this.thumbnail = thumbnail;
		}

		@Override
		public String getTitle() {
			return resource.title;
		}

		@Override
		public String getDate() {
			return resource.date;
		}

		@Override
		public Object getData() {
			return data;
		}

		@Override
		public void releaseData() {
			if (isImageType()) {
				((BitmapDrawable) data).getBitmap().recycle();
			} else if (isVideoType()) {
				// nothing to do
			} else if (isAudioType()) {
				// nothing to do
			}
			data = null;
			if (thumbnail != null) {
				thumbnail.recycle();
			}
			thumbnail = null;
		}

		@Override
		public Caption[] getCaptions() {
			return captions;
		}

		@Override
		public String[] getKeywords() {
			return keywords;
		}

		@Override
		public Bitmap getThumbnail(Context context) {
			if (thumbnail != null) {
				return thumbnail;
			}
			Bitmap b = null;
			if (isImageType()) {
				b = ((BitmapDrawable) data).getBitmap();
				if (b == null) {
					if (sImageIcon == null) {
						sImageIcon = Hilos.getCurrStyleInfo().getBitmap(
								StyleBitmapId.ic_thumb_image);
					}
					b = sImageIcon;
				}
			} else if (isAudioType()) {
				if (sAudioIcon == null) {
					sAudioIcon = Hilos.getCurrStyleInfo().getBitmap(
							StyleBitmapId.ic_thumb_audio);
				}
				b = sAudioIcon;
			} else if (isVideoType()) {
				if (sVideoIcon == null) {
					sVideoIcon = Hilos.getCurrStyleInfo().getBitmap(
							StyleBitmapId.ic_thumb_video);
				}
				b = sVideoIcon;
			}
			return b;
		}

		@Override
		public boolean isImageType() {
			return Resource.isImageType(resource.resourceType);
		}

		@Override
		public boolean isAudioType() {
			return Resource.isAudioType(resource.resourceType);
		}

		@Override
		public boolean isVideoType() {
			return Resource.isVideoType(resource.resourceType);
		}
	}

	/**
	 * A <code>LightBoxItem</code> backed by a resource packaged within the
	 * application.
	 * 
	 * @author Jasleen Singh (jasleen@cs.ucla.edu)
	 * 
	 */
	public abstract static class LocalLightBoxItem implements LightBoxItem {
		private static Bitmap sImageIcon;
		private static Bitmap sAudioIcon;
		private static Bitmap sVideoIcon;
		protected Context context;
		protected int resId;
		protected Caption[] captions;
		protected String[] keywords;
		protected Object data;

		public LocalLightBoxItem(Context context, int resId) {
			this.context = context;
			this.resId = resId;
			this.captions = new Caption[0];
			this.keywords = new String[0];
		}

		@Override
		public String getTitle() {
			return "[title]";
		}

		@Override
		public String getDate() {
			return "[date]";
		}

		@Override
		public Caption[] getCaptions() {
			return captions;
		}

		@Override
		public String[] getKeywords() {
			return keywords;
		}

		@Override
		public Bitmap getThumbnail(Context context) {
			Bitmap b = null;
			if (isImageType()) {
				b = ((BitmapDrawable) data).getBitmap();
				if (b == null) {
					if (sImageIcon == null) {
						sImageIcon = Hilos.getCurrStyleInfo().getBitmap(
								StyleBitmapId.ic_thumb_image);
					}
					b = sImageIcon;
				}
			} else if (isAudioType()) {
				if (sAudioIcon == null) {
					sAudioIcon = Hilos.getCurrStyleInfo().getBitmap(
							StyleBitmapId.ic_thumb_audio);
				}
				b = sAudioIcon;
			} else if (isVideoType()) {
				if (sVideoIcon == null) {
					sVideoIcon = Hilos.getCurrStyleInfo().getBitmap(
							StyleBitmapId.ic_thumb_video);
				}
				b = sVideoIcon;
			}
			return b;
		}

		@Override
		public Object getData() {
			return data;
		}

		public static class Image extends LocalLightBoxItem {
			public Image(Context context, int resId) {
				super(context, resId);
				data = context.getResources().getDrawable(resId);
			}

			@Override
			public boolean isImageType() {
				return true;
			}

			@Override
			public boolean isAudioType() {
				return false;
			}

			@Override
			public boolean isVideoType() {
				return false;
			}

			@Override
			public void releaseData() {
				((BitmapDrawable) data).getBitmap().recycle();
				data = null;
			}
		}
	}
}

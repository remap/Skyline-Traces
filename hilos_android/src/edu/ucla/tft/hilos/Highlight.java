package edu.ucla.tft.hilos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Represents a 'hot spot' in the map view.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Highlight {
	private static class Attributes {
		public final String name;
		public final int skyline_image;
		public final int position_x;
		public final int position_y;
		public final Bitmap bitmap;

		public Attributes(Resource res, double scale) {
			this.name = res.title;
			this.skyline_image = res.getIntAttributeValue("skyline_image");
			Bitmap bitmap = BitmapFactory.decodeStream(Network
					.getResourceInputStream(res.id, res.resourceType, false));
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
					(int) (bitmap.getWidth() * scale),
					(int) (bitmap.getHeight() * scale), false);
			int position_x = res.getIntAttributeValue("position_x");
			int position_y = res.getIntAttributeValue("position_y");
			int scaled_position_x = scale(position_x + skyline_image * 2730
					- bitmap.getWidth() / 2, scale);
			int scaled_position_y = scale(position_y - bitmap.getHeight() / 2,
					scale);
			this.bitmap = scaledBitmap;
			this.position_x = scaled_position_x;
			this.position_y = scaled_position_y;
		}

		private static final int scale(int val, double scale) {
			return (int) (val * scale);
		}
	}

	private Resource type_n;
	private Resource type_s;
	private Resource introVideo;
	private Resource introThumbnail;
	private Resource description;
	private boolean introRun;
	private Attributes attr_n;
	private Attributes attr_s;

	public Highlight(Resource type_n, Resource type_s, double scale,
			Resource introVideo, Resource introThumbnail, Resource description) {
		this.type_n = type_n;
		this.type_s = type_s;
		this.introVideo = introVideo;
		this.introThumbnail = introThumbnail;
		this.description = description;
		attr_n = new Attributes(type_n, scale);
		attr_s = new Attributes(type_s, scale);
	}

	public String associatedWith() {
		return type_n.getStringAttributeValue("parcel_overlay");
	}

	public String name(boolean selected) {
		Attributes attr = selected ? attr_s : attr_n;
		return attr.name;
	}

	public String info() {
		return description == null ? null : description.description;
	}

	public int skyline_image(boolean selected) {
		Attributes attr = selected ? attr_s : attr_n;
		return attr.skyline_image;
	}

	public int positionX(boolean selected) {
		Attributes attr = selected ? attr_s : attr_n;
		return attr.position_x;
	}

	public int positionY(boolean selected) {
		Attributes attr = selected ? attr_s : attr_n;
		return attr.position_y;
	}

	public Bitmap bitmap(boolean selected) {
		Attributes attr = selected ? attr_s : attr_n;
		return attr.bitmap;
	}

	public Resource getIntroVideo() {
		return introVideo;
	}

	public Resource getIntroThumbnail() {
		return introThumbnail;
	}

	public boolean isIntroRun() {
		return introRun;
	}

	public void setIntroRun(boolean introRun) {
		this.introRun = introRun;
	}
}
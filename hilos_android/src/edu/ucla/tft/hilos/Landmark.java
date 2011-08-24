package edu.ucla.tft.hilos;

import org.osmdroid.util.GeoPoint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Represents a 'hot spot' in the skyline view.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class Landmark {
	private static class Attributes {
		public final String name;
		public final GeoPoint location;
		public final int zoom_level;
		public final Bitmap bitmap;

		public Attributes(Resource res) {
			name = res.title;
			location = new GeoPoint(res.latitude, res.longitude);
			zoom_level = res.getIntAttributeValue("zoom_level");
			bitmap = BitmapFactory.decodeStream(Network.getResourceInputStream(
					res.id, res.resourceType, false));
		}
	}

	private Resource type_n;
	private Resource type_s;
	private Resource introVideo;
	private Resource introThumbnail;
	private boolean introRun;
	private Resource description;
	private Attributes attr_n;
	private Attributes attr_s;

	public Landmark(Resource type_n, Resource type_s, Resource introVideo,
			Resource introThumbnail, Resource description) {
		this.type_n = type_n;
		this.type_s = type_s;
		this.introVideo = introVideo;
		this.introThumbnail = introThumbnail;
		this.description = description;
		attr_n = new Attributes(type_n);
		attr_s = new Attributes(type_s);
	}

	public String associatedWith() {
		return type_n.getStringAttributeValue("landmark_overlay");
	}

	public String name(boolean selected) {
		Attributes attr = selected ? attr_s : attr_n;
		return attr.name;
	}

	public String info() {
		return description == null ? null : description.description;
	}

	public GeoPoint location(boolean selected) {
		Attributes attr = selected ? attr_s : attr_n;
		return attr.location;
	}

	public int zoomLevel(boolean selected) {
		Attributes attr = selected ? attr_s : attr_n;
		return attr.zoom_level;
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
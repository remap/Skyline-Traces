package edu.ucla.tft.hilos.maplet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import edu.ucla.tft.hilos.Resource;

/**
 * An overlay item for geo-tagged media items, displayed on the map.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class ResourceOverlayItem extends OverlayItem {
	public static interface OnSelectionChangedListener {
		public void selectionChanged(ResourceOverlayItem[] items,
				boolean selected);
	}

	private static List<ResourceOverlayItem> sSelectedItems = new ArrayList<ResourceOverlayItem>();

	private static Map<String, Drawable> sResourceTypeIconMap;
	private static Map<String, Drawable> sResourceTypeSelectedIconMap;

	public final Resource resource;
	private boolean hidden;

	private Point location = new Point();
	private static OnSelectionChangedListener sListener;

	public ResourceOverlayItem(Resource r) {
		super(r.title, r.description, new GeoPoint(r.latitude, r.longitude));
		this.resource = r;
	}

	public static OnSelectionChangedListener getOnSelectionChangedListener() {
		return sListener;
	}

	public static void setOnSelectionChangedListener(
			OnSelectionChangedListener l) {
		sListener = l;
	}

	protected static void notifySelectionChanged(ResourceOverlayItem[] items,
			boolean selected) {
		if (sListener == null) {
			return;
		}
		sListener.selectionChanged(items, selected);
	}

	public static Map<String, Drawable> getResourceTypeIconMap() {
		return sResourceTypeIconMap;
	}

	public static void setResourceTypeIconMap(Map<String, Drawable> map) {
		sResourceTypeIconMap = map;
	}

	public static Map<String, Drawable> getResourceTypeSelectedIconMap() {
		return sResourceTypeSelectedIconMap;
	}

	public static void setResourceTypeSelectedIconMap(Map<String, Drawable> map) {
		sResourceTypeSelectedIconMap = map;
	}

	public int getX() {
		return location.x;
	}

	public int getY() {
		return location.y;
	}

	public void setLocation(int x, int y) {
		location.set(x, y);
	}

	public static ResourceOverlayItem[] getSelectedItems() {
		return sSelectedItems.toArray(new ResourceOverlayItem[0]);
	}

	public boolean isSelected() {
		return sSelectedItems.contains(this);
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	private static List<ResourceOverlayItem> selectedBatch = new LinkedList<ResourceOverlayItem>();
	private static List<ResourceOverlayItem> deselectedBatch = new LinkedList<ResourceOverlayItem>();

	public void setSelected(boolean selected, boolean batched) {
		if (selected) {
			if (!isSelected()) {
				sSelectedItems.add(this);
				if (batched) {
					selectedBatch.add(this);
				} else {
					notifySelectionChanged(new ResourceOverlayItem[] { this },
							true);
				}
			}
		} else {
			if (isSelected()) {
				sSelectedItems.remove(this);
				if (batched) {
					deselectedBatch.add(this);
				} else {
					notifySelectionChanged(new ResourceOverlayItem[] { this },
							false);
				}
			}
		}
	}

	public static void notifyBatchSelection() {
		if (deselectedBatch.size() > 0) {
			notifySelectionChanged(
					deselectedBatch.toArray(new ResourceOverlayItem[0]), false);
			deselectedBatch.clear();
		}
		if (selectedBatch.size() > 0) {
			notifySelectionChanged(
					selectedBatch.toArray(new ResourceOverlayItem[0]), true);
			selectedBatch.clear();
		}
	}

	@Override
	public Drawable getDrawable() {
		Map<String, Drawable> map = isSelected() ? sResourceTypeSelectedIconMap
				: sResourceTypeIconMap;
		return map == null ? null : map.get(resource.resourceType);
	}
}

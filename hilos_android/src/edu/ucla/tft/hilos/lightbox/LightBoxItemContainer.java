package edu.ucla.tft.hilos.lightbox;

/**
 * An interface for classes displaying {@link LightBoxItem}s. Provides methods
 * to access each item as well as register listeners interested in selection
 * status changes.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public interface LightBoxItemContainer {
	public static interface Listener {
		public void selectionChanged();
	}

	public LightBoxItemProvider getProvider();

	public void setProvider(LightBoxItemProvider provider);

	public LightBoxItem getLightBoxItemAt(int index);

	public int getLightBoxItemSelected();

	public void setLightBoxItemSelected(int index);

	public int getLightBoxItemsCount();

	public void releaseLightBoxItems();

	public void onLaunchGesture();

	public void addListener(Listener l);

	public void removeListener(Listener l);
}

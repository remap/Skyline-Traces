package edu.ucla.tft.hilos.lightbox;

import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import edu.ucla.tft.hilos.Network;
import edu.ucla.tft.hilos.Resource;
import edu.ucla.tft.hilos.ResourceFilter;
import edu.ucla.tft.hilos.ThumbnailProvider;
import edu.ucla.tft.hilos.lightbox.LightBoxItem.LocalLightBoxItem;
import edu.ucla.tft.hilos.lightbox.LightBoxItem.ResourceLightBoxItem;

/**
 * A interface for {@link LightBoxItem} providers. This interface abstracts away
 * the particular method used to create a <code>LightBoxItem</code>. For
 * instance, a provider fetching images from a remote server will download the
 * image, package it within a <code>LightBoxItem</code> object and notify the
 * consumer when it is ready. A local provider might just load the image off the
 * disk instead. The consumer doesn't (and shouldn't) care where the particular
 * resource comes from, and even what is its type (image, audio or video).
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public interface LightBoxItemProvider extends Serializable {
	public static interface LoadCallback {
		public void done(int index, LightBoxItem item);

		public void error(int index, String message);
	}

	public void loadLightBoxItemAt(Context context, int index,
			LoadCallback callback);

	public boolean isLoading();

	public int getLightBoxItemsCount();

	public static class ResourceLightBoxItemProvider implements
			LightBoxItemProvider {
		private static Handler sLoader;
		static {
			HandlerThread t = new HandlerThread(
					"ResourceLightBoxItemProviderHandlerThread",
					HandlerThread.MIN_PRIORITY);
			t.start();
			sLoader = new Handler(t.getLooper());
		}

		private boolean loading;

		private Resource[] resources;

		private Resource[] filtered;

		private ResourceFilter filter;

		public ResourceLightBoxItemProvider() {
			this(null);
		}

		public ResourceLightBoxItemProvider(Resource[] resources) {
			this.resources = resources;
		}

		public ResourceFilter getFilter() {
			return filter;
		}

		public void setFilter(ResourceFilter filter) {
			this.filter = filter;
		}

		private synchronized Resource[] getResources() {
			if (resources == null) {
				resources = Network.getResourceList(null, false);
			}
			if (filtered == null) {
				if (filter == null) {
					filtered = resources;
				} else {
					List<Resource> filteredList = new LinkedList<Resource>();
					for (Resource res : resources) {
						if (filter.accept(res)) {
							filteredList.add(res);
						}
					}
					filtered = filteredList.toArray(new Resource[0]);
				}
			}
			return filtered;
		}

		@Override
		public void loadLightBoxItemAt(final Context ctx, final int index,
				final LoadCallback lc) {
			loading = true;
			sLoader.post(new Runnable() {
				@Override
				public void run() {
					try {
						Resource[] resources = getResources();
						if (index >= resources.length) {
							return;
						}
						Thread.sleep(100);
						Resource res = resources[index];
						Object data = null;
						Bitmap thumbnail = null;
						if (Resource.isImageType(res.resourceType)) {
							InputStream in = Network.getResourceInputStream(
									res.id, res.resourceType, false);
							data = new BitmapDrawable(in);
							in.close();
						} else if (Resource.isAudioType(res.resourceType)) {
							data = Network.getResourceURL(res.id,
									res.resourceType);
						} else if (Resource.isVideoType(res.resourceType)) {
							data = Network.getResourceURL(res.id,
									res.resourceType);
						}
						if (res instanceof ThumbnailProvider) {
							Resource thumbRes = ((ThumbnailProvider) res)
									.getThumbnail();
							InputStream in = Network.getResourceInputStream(
									thumbRes.id, thumbRes.resourceType, false);
							thumbnail = BitmapFactory.decodeStream(in);
							in.close();
						}
						LightBoxItem item = new ResourceLightBoxItem(res, data,
								thumbnail);
						lc.done(index, item);
					} catch (Exception e) {
						e.printStackTrace();
						lc.error(index, e.getMessage());
					}
					loading = false;
				}
			});
		}

		@Override
		public boolean isLoading() {
			return loading;
		}

		@Override
		public int getLightBoxItemsCount() {
			return getResources().length;
		}
	}

	public static class LocalLightBoxItemProvider implements
			LightBoxItemProvider {
		private int[] mResIds = new int[] {};
		private LightBoxItem[] mLightBoxItems = new LightBoxItem[mResIds.length];

		@Override
		public void loadLightBoxItemAt(Context context, int index,
				LoadCallback callback) {
			mLightBoxItems[index] = new LocalLightBoxItem.Image(context,
					mResIds[index]);
			callback.done(index, mLightBoxItems[index]);
		}

		@Override
		public boolean isLoading() {
			return false;
		}

		@Override
		public int getLightBoxItemsCount() {
			return mLightBoxItems.length;
		}
	}
}

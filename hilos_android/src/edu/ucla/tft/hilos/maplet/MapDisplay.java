package edu.ucla.tft.hilos.maplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.ucla.tft.hilos.About;
import edu.ucla.tft.hilos.AbstractToolbar;
import edu.ucla.tft.hilos.AppSettings;
import edu.ucla.tft.hilos.BaseActivity;
import edu.ucla.tft.hilos.BaseView;
import edu.ucla.tft.hilos.Help;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.Keywords;
import edu.ucla.tft.hilos.Landmark;
import edu.ucla.tft.hilos.Network;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.Resource;
import edu.ucla.tft.hilos.ResourceFilterByAttribute;
import edu.ucla.tft.hilos.Splash;
import edu.ucla.tft.hilos.StyleInfo;
import edu.ucla.tft.hilos.StyleInfo.SoundId;
import edu.ucla.tft.hilos.StyleInfo.StyleBitmapId;
import edu.ucla.tft.hilos.ThumbnailProvider.Resource_wThumbnail;
import edu.ucla.tft.hilos.Utils;
import edu.ucla.tft.hilos.cloudscape.Cloudscape;
import edu.ucla.tft.hilos.lightbox.LightBoxItem;
import edu.ucla.tft.hilos.lightbox.LightBoxItemContainer;
import edu.ucla.tft.hilos.lightbox.LightBoxItemProvider.ResourceLightBoxItemProvider;
import edu.ucla.tft.hilos.lightbox.LightBoxMini;
import edu.ucla.tft.hilos.panoromeo.PanoDisplay;
import edu.ucla.tft.hilos.panoromeo.PanoToolbar;

/**
 * Activity for displaying a custom map. The view is composed of a number of
 * 'layers' which provide assorted information about the current item of
 * interest. Also serves as a listener for GPS coordinate changes and updates
 * the position of the current location. A 'virtual' mode allows the user to
 * change the current location by dragging the map.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class MapDisplay extends BaseActivity implements LocationListener,
		SensorListener, GlassPane.Listener, MapToolbar.Listener,
		LightBoxItemContainer.Listener,
		ResourceOverlayItem.OnSelectionChangedListener,
		LandmarkOverlay.OnSelectionChangedListener {
	private SensorManager sensorManager;
	private LocationManager locationManager;

	private MapView mMapView;
	private MyLocationOverlay mMyLocationOverlay;
	private GlassPane mGlassPane;
	private Cloudscape mCloudscape;
	private MapToolbar mToolbar;
	private TextView mDebugText;
	private boolean mPinned;
	private OnTouchListener mHighlightLandmarkTouchListener = new OnTouchListener() {
		private Toast mToast;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				for (LandmarkOverlay lo : mLandmarkOverlays) {
					Projection pj = mMapView.getProjection();
					if (lo.isWithinBitmap(mMapView,
							pj.fromPixels(event.getX(), event.getY()))) {
						String info = lo.landmark.info();
						if (info != null) {
							if (mToast != null) {
								mToast.cancel();
							}
							mToast = Toast.makeText(MapDisplay.this, info,
									Toast.LENGTH_LONG);
							mToast.show();
						}
						lo.setHighlighted(true);
					} else {
						lo.setHighlighted(false);
					}
				}
			}
			mMapView.postInvalidate();
			return false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		StyleInfo styleInfo = Hilos.getCurrStyleInfo();
		setTheme(styleInfo.systemTheme);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;

		// GeoPoint forceLocationToHome = Hilos.getForceLocationToHome();
		// sCurrentLocationOffset.setCoordsE6(
		// sDefaultHome.getLatitudeE6()
		// - forceLocationToHome.getLatitudeE6(),
		// sDefaultHome.getLongitudeE6()
		// - forceLocationToHome.getLongitudeE6());

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		Map<String, Drawable> resourceTypeIconMap = new HashMap<String, Drawable>();
		Map<String, Drawable> resourceTypeSelectedIconMap = new HashMap<String, Drawable>();
		BitmapDrawable ic, ic_sel;
		ic = currStyleInfo.getBitmapDrawable(StyleBitmapId.ic_overlay_image);
		ic_sel = currStyleInfo
				.getBitmapDrawable(StyleBitmapId.ic_overlay_image_selected);
		for (String s : Resource.sResourceTypeImageList) {
			resourceTypeIconMap.put(s, ic);
			resourceTypeSelectedIconMap.put(s, ic_sel);
		}
		ic = currStyleInfo.getBitmapDrawable(StyleBitmapId.ic_overlay_audio);
		ic_sel = currStyleInfo
				.getBitmapDrawable(StyleBitmapId.ic_overlay_audio_selected);
		for (String s : Resource.sResourceTypeAudioList) {
			resourceTypeIconMap.put(s, ic);
			resourceTypeSelectedIconMap.put(s, ic_sel);
		}
		ic = currStyleInfo.getBitmapDrawable(StyleBitmapId.ic_overlay_video);
		ic_sel = currStyleInfo
				.getBitmapDrawable(StyleBitmapId.ic_overlay_video_selected);
		for (String s : Resource.sResourceTypeVideoList) {
			resourceTypeIconMap.put(s, ic);
			resourceTypeSelectedIconMap.put(s, ic_sel);
		}
		ResourceOverlayItem.setResourceTypeIconMap(resourceTypeIconMap);
		ResourceOverlayItem
				.setResourceTypeSelectedIconMap(resourceTypeSelectedIconMap);

		FrameLayout view = (FrameLayout) LayoutInflater.from(this).inflate(
				R.layout.map_display, null);
		mMapView = (MapView) view.findViewById(R.id.mapview);
		mMapView.setTileSource(new HilosXYTileSource("Hilos-"
				+ Hilos.getStyleIndex(), ResourceProxy.string.mapnik, 0, 18,
				256, ".png", String.format("%s?si=%d&rp=", Hilos.getHostname()
						+ "/getTile", Hilos.getStyleIndex())));
		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(false);
		mMapView.setMultiTouchControls(true);
		mMapView.setOnTouchListener(mHighlightLandmarkTouchListener);
		ResourceProxy resProxy = new DefaultResourceProxyImpl(this);
		mMyLocationOverlay = new MyLocationOverlay(this, mMapView, resProxy) {
			private Drawable mPinnedLocationDrawable = getResources()
					.getDrawable(R.drawable.ic_pinned_location);
			private Matrix mPinnedLocationMatrix = new Matrix();

			public void draw(Canvas c, MapView osmv, boolean shadow) {
				if (isPinned()) {
					if (shadow) {
						return;
					}
					c.save();
					c.setMatrix(mPinnedLocationMatrix);
					int width = osmv.getWidth();
					int height = osmv.getHeight();
					mPinnedLocationDrawable
							.setBounds(width / 2 - 100, height / 2 - 100,
									width / 2 + 100, height / 2 + 100);
					mPinnedLocationDrawable.draw(c);
					c.restore();
				} else {
					super.draw(c, osmv, shadow);
				}
			}

			public void onLocationChanged(Location location) {
				location.setLatitude(location.getLatitude()
						+ (double) sCurrentLocationOffset.getLatitudeE6() / 1e6);
				location.setLongitude(location.getLongitude()
						+ (double) sCurrentLocationOffset.getLongitudeE6()
						/ 1e6);
				// location.setLatitude((double) sDefaultHome.getLatitudeE6() /
				// 1e6);
				// location.setLongitude((double) sDefaultHome.getLongitudeE6()
				// / 1e6);
				mVirtualLocation.setCoordsE6(
						(int) (location.getLatitude() * 1e6),
						(int) (location.getLongitude() * 1e6));
				if (!isPinned()) {
					mMapView.getController().animateTo(mVirtualLocation);
				}
				super.onLocationChanged(location);
			}
		};
		mMyLocationOverlay.enableCompass();
		mMyLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mMapView.getController().animateTo(
						mMyLocationOverlay.getMyLocation());
			}
		});

		mGlassPane = (GlassPane) view.findViewById(R.id.glasspane);
		mGlassPane.setListener(this);
		mGlassPane.setDispatchTouchEventsTo(mMapView);

		mToolbar = (MapToolbar) view.findViewById(R.id.map_toolbar);
		mToolbar.setListener(this);

		mLightBoxMini = LayoutInflater.from(this).inflate(
				R.layout.lightbox_mini, null);
		mLightBoxMini.setVisibility(View.INVISIBLE);
		view.addView(mLightBoxMini);

		mCloudscape = (Cloudscape) LayoutInflater.from(this).inflate(
				R.layout.cloudscape, null);
		view.addView(mCloudscape);

		float scaleView = (float) Math.min(screenWidth, screenHeight)
				/ Math.min(BaseView.BASE_WIDTH, BaseView.BASE_HEIGHT);
		LightBoxMini lightBoxMini = (LightBoxMini) view
				.findViewById(R.id.lightBoxMini);
		lightBoxMini.addListener(this);
		int lightBoxSize = (int) (300 * scaleView);
		lightBoxMini.getLayoutParams().width = lightBoxSize;
		lightBoxMini.getLayoutParams().height = lightBoxSize;
		TextView miniTitle = (TextView) view.findViewById(R.id.mini_title);
		miniTitle.setTextSize(10 * scaleView);

		mDebugText = (TextView) view.findViewById(R.id.debugText);
		if (Hilos.isDebug()) {
			runDebugThread();
		} else {
			mDebugText.setVisibility(View.INVISIBLE);
		}
		setContentView(view);
	}

	@Override
	protected String getBackgroundSoundPath() {
		return Hilos.getCurrStyleInfo().getSoundFilePath(SoundId.map);
	}

	protected int getBackgroundSoundBehavior() {
		return BGSOUND_START;
	}

	@Override
	public void onBackPressed() {
		if (mCloudscape.getState() == Cloudscape.HELD) {
			mCloudscape.dismiss();
		} else {
			Intent intent = new Intent(this, Splash.class);
			startActivity(intent);
			finish();
		}
	}

	public boolean isPinned() {
		return mPinned;
	}

	public void setPinned(boolean pinned) {
		this.mPinned = pinned;
	}

	private static final int FORCE_CURR_LOCATION_TO_HOME = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean b = super.onCreateOptionsMenu(menu);
		if (Hilos.isDebug()) {
			menu.add(Menu.NONE, FORCE_CURR_LOCATION_TO_HOME, Menu.NONE,
					"Force Home");
			b = true;
		}
		return b;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case FORCE_CURR_LOCATION_TO_HOME:
			sCurrentLocationOffset.setCoordsE6(
					sDefaultHome.getLatitudeE6()
							- mRealLocation.getLatitudeE6(),
					sDefaultHome.getLongitudeE6()
							- mRealLocation.getLongitudeE6());
			Hilos.setForceLocationToHome(mRealLocation);
			((Hilos) getApplication()).saveSettings();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void runDebugThread() {
		new Thread() {
			public void run() {
				while (true) {
					mDebugText.post(new Runnable() {
						public void run() {
							mDebugText.setText(mMapView
									.getMapCenterLatitudeE6()
									+ ", "
									+ mMapView.getMapCenterLongitudeE6());
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
	}

	@Override
	public void selectionChanged() {
		LightBoxMini lightBoxMini = (LightBoxMini) mLightBoxMini
				.findViewById(R.id.lightBoxMini);
		LightBoxItem item = lightBoxMini.getLightBoxItemAt(lightBoxMini
				.getLightBoxItemSelected());
		if (item != null) {
			mSpeechEngine.speakImmediately(item.getTitle());
		}
	}

	@Override
	public void selectionChanged(ResourceOverlayItem[] items, boolean selected) {
		if (selected) {
			mCloudscape.clear();
			Keywords[] keywordsArray = new Keywords[items.length];
			for (int i = 0; i < items.length; i++) {
				keywordsArray[i] = items[i].resource.keywords;
			}
			mCloudscape
					.addKeywords(Utils.getTopKeywordNames(keywordsArray, 10));
			if (mLightBoxMini.getVisibility() != View.VISIBLE) {
				mLightBoxMini.setVisibility(View.VISIBLE);
			}
			List<Resource> overlayResources = new LinkedList<Resource>();
			for (int i = 0; i < mResources.length; i++) {
				for (int j = 0; j < items.length; j++) {
					if (mResources[i] == items[j].resource) {
						overlayResources.add(mResources[i]);
					}
				}
			}
			switch (Hilos.getShowLandmarksUsingIndex()) {
			case 0:
				LandmarkOverlay selectedLandmarkOverlay = LandmarkOverlay
						.getSelected();
				Resource introVideo = selectedLandmarkOverlay.landmark
						.getIntroVideo();
				Resource introThumbnail = selectedLandmarkOverlay.landmark
						.getIntroThumbnail();
				if (introVideo != null
						&& !selectedLandmarkOverlay.landmark.isIntroRun()) {
					Resource introRes = new Resource_wThumbnail(introVideo,
							introThumbnail);
					overlayResources.add(0, introRes);
				}
				break;
			}
			if (overlayResources.isEmpty()) {
				mLightBoxMini.setVisibility(View.INVISIBLE);
			} else {
				if (mLightBoxMini.getVisibility() != View.VISIBLE) {
					mLightBoxMini.setVisibility(View.VISIBLE);
				}
				LightBoxMini lightBoxMini = (LightBoxMini) mLightBoxMini
						.findViewById(R.id.lightBoxMini);
				lightBoxMini.setProvider(new ResourceLightBoxItemProvider(
						overlayResources.toArray(new Resource[0])));
			}
		} else if (ResourceOverlayItem.getSelectedItems().length == 0) {
			mCloudscape.clear();
			mLightBoxMini.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void selectionChanged(LandmarkOverlay landmarkOverlay,
			boolean selected) {
		if (selected) {
			playEarcon(EC_START);
			vibrate(1000);
			mSpeechEngine.speakImmediately(landmarkOverlay.landmark
					.name(selected));
			switch (Hilos.getShowLandmarksUsingIndex()) {
			case 0:
				for (ResourceOverlayItem item : mResourceOverlayItems) {
					String associatedWith = landmarkOverlay.landmark
							.associatedWith();
					if (item.resource.matchesAttribute("landmark",
							associatedWith)) {
						item.setSelected(true, true);
					}
				}
				ResourceOverlayItem.notifyBatchSelection();
				break;
			}
			landmarkOverlay.landmark.setIntroRun(true);
		} else {
			switch (Hilos.getShowLandmarksUsingIndex()) {
			case 0:
				for (ResourceOverlayItem item : mResourceOverlayItems) {
					String associatedWith = landmarkOverlay.landmark
							.associatedWith();
					if (item.resource.matchesAttribute("landmark",
							associatedWith)) {
						item.setSelected(false, false);
					}
				}
				ResourceOverlayItem.notifyBatchSelection();
				break;
			}
			playEarcon(EC_FINISH);
			vibrate(1000);
		}
	}

	private boolean doneOnce = false;
	private static final int sDefaultZoomLevel = 17;
	private static final GeoPoint sDefaultHome = new GeoPoint(34067360,
			-118233747);
	private static final GeoPoint sCurrentLocationOffset = new GeoPoint(0, 0);

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && !doneOnce) {
			MapController mc = mMapView.getController();
			mc.setZoom(sDefaultZoomLevel);
			mMapView.getController().setCenter(sDefaultHome);
			addOverlays();
			doneOnce = true;
		}
	}

	private Resource[] mResources;
	private List<ResourceOverlayItem> mResourceOverlayItems = new ArrayList<ResourceOverlayItem>();
	private List<LandmarkOverlay> mLandmarkOverlays = new ArrayList<LandmarkOverlay>();

	private void addOverlays() {
		new Thread() {
			@Override
			public void run() {
				try {
					loadLandmarks();
					loadResources();
					mMapView.post(new Runnable() {
						@Override
						public void run() {
							for (LandmarkOverlay lo : mLandmarkOverlays) {
								mMapView.getOverlays().add(lo);
							}
							mMapView.getOverlays()
									.add(createResourceOverlays());
							mMapView.getOverlays().add(mMyLocationOverlay);
						}
					});
					mMapView.postInvalidate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private ItemizedOverlay<ResourceOverlayItem> createResourceOverlays() {
		final ItemizedOverlay<ResourceOverlayItem> itemizedOverlay = new ItemizedOverlay<ResourceOverlayItem>(
				MapDisplay.this, mResourceOverlayItems,
				new OnItemGestureListener<ResourceOverlayItem>() {
					@Override
					public boolean onItemLongPress(int arg0,
							ResourceOverlayItem arg1) {
						return true;
					}

					@Override
					public boolean onItemSingleTapUp(int arg0,
							ResourceOverlayItem item) {
						if (item.isHidden()) {
							return false;
						}
						ResourceOverlayItem[] selection = ResourceOverlayItem
								.getSelectedItems();
						if (selection.length > 1) {
							for (ResourceOverlayItem sel : selection) {
								if (sel == item) {
									continue;
								}
								sel.setSelected(false, true);
							}
							ResourceOverlayItem.notifyBatchSelection();
						} else if (selection.length == 1
								&& selection[0] != item) {
							selection[0].setSelected(false, false);
						}
						item.setSelected(!item.isSelected(), false);
						playEarcon(EC_TOCK);
						mMapView.postInvalidate();
						return true;
					}
				}, mMapView.getResourceProxy()) {

			private Rect bounds = new Rect();
			private final boolean hideAllItems = true;

			protected void onDrawItem(Canvas canvas, int index,
					Point curScreenCoords) {
				ResourceOverlayItem item = mItemList.get(index);
				if (hideAllItems) {
					item.setHidden(true);
					return;
				}
				GeoPoint refPoint = isPinned() ? mMapView.getMapCenter()
						: mVirtualLocation;
				item.setHidden(!showItem(item, refPoint));
				if (item.isHidden()) {
					return;
				}
				item.setLocation(curScreenCoords.x, curScreenCoords.y);
				Drawable d = item.getDrawable();
				bounds.left = item.getX() - 44;
				bounds.right = bounds.left + 64;
				bounds.top = item.getY() - 60;
				bounds.bottom = bounds.top + 64;
				d.setBounds(bounds.left, bounds.top, bounds.right,
						bounds.bottom);
				d.draw(canvas);
			}

			private boolean showItem(ResourceOverlayItem item, GeoPoint refPoint) {
				if (!Hilos.isTrackLocation()) {
					return true;
				}
				switch (Hilos.getShowLandmarksUsingIndex()) {
				case 0:
					LandmarkOverlay lo = LandmarkOverlay.getSelected();
					if (lo == null) {
						return false;
					}
					String associatedWith = lo.landmark.associatedWith();
					return item.resource.matchesAttribute("landmark",
							associatedWith);
				case 1:
					return item.getPoint().distanceTo(refPoint) <= Hilos
							.getProximityRadius();
				}
				return false;
			}
		};
		return itemizedOverlay;
	}

	private void loadResources() {
		ResourceOverlayItem.setOnSelectionChangedListener(MapDisplay.this);
		mResources = Network.getResourceList(new ResourceFilterByAttribute(
				"landmark", "-landmark_video", "-landmark_thumbnail",
				"-landmark_description"), false);
		mResourceOverlayItems.clear();
		for (Resource res : mResources) {
			ResourceOverlayItem roi = new ResourceOverlayItem(res);
			mResourceOverlayItems.add(roi);
		}
	}

	private void loadLandmarks() {
		// Load landmarks
		LandmarkOverlay.setOnSelectionChangedListener(MapDisplay.this);
		Resource[] landmarkResources = Network.getResourceList(
				new ResourceFilterByAttribute("landmark_overlay", "style:"
						+ Hilos.getStyleIndex()), false);
		Resource[] landmarkVideos = Network.getResourceList(
				new ResourceFilterByAttribute("landmark_video"), false);
		Resource[] landmarkThumbnails = Network.getResourceList(
				new ResourceFilterByAttribute("landmark_thumbnail"), false);
		LandmarkOverlay lo;
		mLandmarkOverlays.clear();
		for (Resource type_n : Utils.filterResources(landmarkResources,
				new ResourceFilterByAttribute("type:n"))) {
			String landmarkName = type_n
					.getStringAttributeValue("landmark_overlay");
			Resource type_s = Utils.findResourceByAttribute(landmarkResources,
					new ResourceFilterByAttribute("landmark_overlay:"
							+ landmarkName, "type:s"));
			Resource introVideo = Utils.findResourceByAttribute(landmarkVideos,
					new ResourceFilterByAttribute("landmark_video:"
							+ landmarkName));
			Resource introThumbnail = Utils.findResourceByAttribute(
					landmarkThumbnails, new ResourceFilterByAttribute(
							"landmark_thumbnail:" + landmarkName));
			Resource landmarkDescription = Utils.findResourceByAttribute(
					landmarkVideos, new ResourceFilterByAttribute(
							"landmark_description:" + landmarkName));
			lo = new LandmarkOverlay(MapDisplay.this, new Landmark(type_n,
					type_s, introVideo, introThumbnail, landmarkDescription));
			synchronized (mLandmarkOverlays) {
				mLandmarkOverlays.add(lo);
			}
		}
	}

	private GeoPoint mVirtualLocation = new GeoPoint(0, 0);

	public GeoPoint getVirtualLocation() {
		return mVirtualLocation;
	}

	private GeoPoint mRealLocation = new GeoPoint(0, 0);

	public GeoPoint RealLocation() {
		return mRealLocation;
	}

	public void startListening() {
		for (final String provider : locationManager.getProviders(true)) {
			if (LocationManager.GPS_PROVIDER.equals(provider)
					|| LocationManager.NETWORK_PROVIDER.equals(provider)) {
				locationManager.requestLocationUpdates(provider, 0, 0, this);
			}
		}
	}

	public void stopListening() {
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		mRealLocation.setCoordsE6((int) (location.getLatitude() * 1e6),
				(int) (location.getLongitude() * 1e6));
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void selectionRectChanged(Rect rect) {
		for (ResourceOverlayItem item : mResourceOverlayItems) {
			int x = item.getX() - mMapView.getScrollX() + mMapView.getWidth()
					/ 2;
			int y = item.getY() - mMapView.getScrollY() + mMapView.getHeight()
					/ 2;
			item.setSelected(rect.contains(x, y), true);
		}
		ResourceOverlayItem.notifyBatchSelection();
		mMapView.postInvalidate();
	}

	@Override
	public void onButtonAction(AbstractToolbar toolbar, int button, int action) {
		if (toolbar instanceof MapToolbar) {
			switch (button) {
			case MapToolbar.HOME:
				if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
					mMapView.getController().setZoom(sDefaultZoomLevel);
					mMapView.getController().animateTo(sDefaultHome);
					updateToolbarZoomButtonStates();
				}
				break;
			case MapToolbar.INFO:
				if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
					Intent intent = new Intent(this, About.class);
					startActivity(intent);
				}
				break;
			case MapToolbar.HELP:
				if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
					Intent intent = new Intent(this, Help.class);
					startActivity(intent);
				}
				break;
			case MapToolbar.PIN:
				if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
					setPinned(!isPinned());
					mToolbar.setButtonSelected(PanoToolbar.PIN, isPinned());
				}
				break;
			// case MapToolbar.GALERIE:
			// if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
			// Intent intent = new Intent(this, Galerie.class);
			// startActivity(intent);
			// }
			// break;
			case MapToolbar.ZOOMIN:
				if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
					mMapView.getController().zoomIn();
					updateToolbarZoomButtonStates();
				}
				break;
			case MapToolbar.ZOOMOUT:
				if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
					mMapView.getController().zoomOut();
					updateToolbarZoomButtonStates();
				}
				break;
			}
		}
	}

	private void updateToolbarZoomButtonStates() {
		mToolbar.setButtonEnabled(MapToolbar.ZOOMIN,
				mMapView.getZoomLevel() != mMapView.getMaxZoomLevel());
		mToolbar.setButtonEnabled(MapToolbar.ZOOMOUT,
				mMapView.getZoomLevel() != mMapView.getMinZoomLevel());
	}

	private long lastChange;
	private View mLightBoxMini;

	@Override
	public void onSensorChanged(int sensor, float[] values) {
		switch (sensor) {
		case SensorManager.SENSOR_ORIENTATION:
			float val = Math.abs(values[1]);
			long n = System.currentTimeMillis();
			if ((n - lastChange > 1000) && val > 75) {
				n = lastChange;
				startActivity(new Intent(this, PanoDisplay.class));
			}
			break;
		}
	}

	@Override
	protected void onStop() {
		mMapView.getTileProvider().clearTileCache();
		super.onStop();
	}

	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCloudscape.start();
		sensorManager.registerListener(this, SensorManager.SENSOR_ORIENTATION);
		if (Hilos.isTrackLocation()) {
			mMyLocationOverlay.enableMyLocation();
			startListening();
		}
	}

	@Override
	protected void onPause() {
		mCloudscape.stop();
		sensorManager.unregisterListener(this);
		if (Hilos.isTrackLocation()) {
			mMyLocationOverlay.disableMyLocation();
			stopListening();
		}
		super.onStop();
	}
}

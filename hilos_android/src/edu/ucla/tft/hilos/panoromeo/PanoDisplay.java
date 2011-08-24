package edu.ucla.tft.hilos.panoromeo;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import edu.ucla.tft.hilos.About;
import edu.ucla.tft.hilos.AbstractToolbar;
import edu.ucla.tft.hilos.BaseActivity;
import edu.ucla.tft.hilos.BaseView;
import edu.ucla.tft.hilos.Help;
import edu.ucla.tft.hilos.Highlight;
import edu.ucla.tft.hilos.Network;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.Resource;
import edu.ucla.tft.hilos.ResourceFilterByAttribute;
import edu.ucla.tft.hilos.ThumbnailProvider.Resource_wThumbnail;
import edu.ucla.tft.hilos.Utils;
import edu.ucla.tft.hilos.citylens.CityLens;
import edu.ucla.tft.hilos.lightbox.LightBoxItem;
import edu.ucla.tft.hilos.lightbox.LightBoxItemContainer;
import edu.ucla.tft.hilos.lightbox.LightBoxItemProvider.ResourceLightBoxItemProvider;
import edu.ucla.tft.hilos.lightbox.LightBoxMini;
import edu.ucla.tft.hilos.maplet.MapDisplay;
import edu.ucla.tft.hilos.maplet.MapToolbar;
import edu.ucla.tft.hilos.panoromeo.PanoDisplayView.OnHighlightSelectionChangedListener;

/**
 * Activity for displaying the skyline. The view is updated depending on which
 * direction the user if facing.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class PanoDisplay extends BaseActivity implements SensorListener,
		OnHighlightSelectionChangedListener, PanoToolbar.Listener,
		LightBoxItemContainer.Listener {
	private SensorManager sensorManager;
	private PanoDisplayView mPanoDisplayView;
	private View mLightBoxMini;
	private PanoToolbar mToolbar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mPanoDisplayView = (PanoDisplayView) LayoutInflater.from(this).inflate(
				R.layout.pano_display, null);
		mLightBoxMini = LayoutInflater.from(this).inflate(
				R.layout.lightbox_mini, null);
		mLightBoxMini.setVisibility(View.INVISIBLE);
		mPanoDisplayView.addView(mLightBoxMini);

		mToolbar = (PanoToolbar) mPanoDisplayView
				.findViewById(R.id.pano_toolbar);
		mToolbar.setListener(this);

		float scaleView = (float) Math.min(screenWidth, screenHeight)
				/ Math.min(BaseView.BASE_WIDTH, BaseView.BASE_HEIGHT);
		LightBoxMini lightBoxMini = (LightBoxMini) mPanoDisplayView
				.findViewById(R.id.lightBoxMini);
		lightBoxMini.addListener(this);
		int lightBoxSize = (int) (300 * scaleView);
		lightBoxMini.getLayoutParams().width = lightBoxSize;
		lightBoxMini.getLayoutParams().height = lightBoxSize;
		TextView miniTitle = (TextView) mPanoDisplayView
				.findViewById(R.id.mini_title);
		miniTitle.setTextSize(10 * scaleView);

		mPanoDisplayView.setOnHighlightSelectionChangedListener(this);
		setContentView(mPanoDisplayView);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mPanoDisplayView.initResources();
	}

	@Override
	public void onDestroy() {
		mPanoDisplayView.releaseResources();
		super.onDestroy();
	}
	
	@Override
	public void onButtonAction(AbstractToolbar toolbar, int button, int action) {
		if (toolbar instanceof PanoToolbar) {
			switch (button) {
			case PanoToolbar.HOME:
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
			case PanoToolbar.PIN:
				if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
					mPanoDisplayView.setPinned(!mPanoDisplayView.isPinned());
					mToolbar.setButtonSelected(PanoToolbar.PIN,
							mPanoDisplayView.isPinned());
				}
				break;
			case PanoToolbar.CITYLENS:
				if (action == AbstractToolbar.Listener.ACTION_SELECTED) {
					startActivity(new Intent(this, CityLens.class));
				}
				break;
			}
		}
	}

	private boolean doneOnce = false;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && !doneOnce) {
			loadResources();
			doneOnce = true;
		}
	}

	private Resource[] mResources;

	private void loadResources() {
		new Thread() {
			@Override
			public void run() {
				mResources = Network.getResourceList(
						new ResourceFilterByAttribute("parcel",
								"-parcel_video", "-parcel_thumbnail",
								"-parcel_description"), false);
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
	public void highlightSelectionChanged(Highlight highlight, boolean selected) {
		if (selected) {
			List<Resource> highlightResources = new LinkedList<Resource>();
			highlightResources.addAll(Arrays.asList(Utils.filterResources(
					mResources, new ResourceFilterByAttribute("parcel:"
							+ highlight.associatedWith()))));
			Resource introVideo = highlight.getIntroVideo();
			Resource introThumbnail = highlight.getIntroThumbnail();
			if (introVideo != null && !highlight.isIntroRun()) {
				Resource introRes = new Resource_wThumbnail(introVideo,
						introThumbnail);
				highlightResources.add(0, introRes);
			}
			if (highlightResources.isEmpty()) {
				mLightBoxMini.setVisibility(View.INVISIBLE);
			} else {
				if (mLightBoxMini.getVisibility() != View.VISIBLE) {
					mLightBoxMini.setVisibility(View.VISIBLE);
				}
				LightBoxMini lightBoxMini = (LightBoxMini) mLightBoxMini
						.findViewById(R.id.lightBoxMini);
				lightBoxMini.setProvider(new ResourceLightBoxItemProvider(
						highlightResources.toArray(new Resource[0])));
			}
		}
	}

	private long lastChange;

	@Override
	public void onSensorChanged(int sensor, float[] values) {
		switch (sensor) {
		case SensorManager.SENSOR_ORIENTATION:
			mPanoDisplayView.calculateOffset(values[0]);
			float val = Math.abs(values[1]);
			long n = System.currentTimeMillis();
			if ((n - lastChange > 1000) && val < 15) {
				n = lastChange;
				finish();
			}
			break;
		}
	}

	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, SensorManager.SENSOR_ORIENTATION);
	}

	@Override
	protected void onPause() {
		sensorManager.unregisterListener(this);
		super.onStop();
	}
}
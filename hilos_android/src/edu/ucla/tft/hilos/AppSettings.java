package edu.ucla.tft.hilos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * Activity for displaying and modifying application settings. Each setting has
 * a corresponding static variable in the Hilos class. Lifecycle methods
 * (onResume, onPause) are used to read and write to these variables.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class AppSettings extends PreferenceActivity {
	public static final String DEBUG_KEY = "debug";
	public static final String LANGUAGE_KEY = "language";
	public static final String HOSTNAME_KEY = "hostname";
	public static final String OFFLINE_MODE_KEY = "offline_mode";
	public static final String CACHE_KEY = "cache";
	public static final String STYLE_KEY = "style";
	public static final String SOUND_VIBRATION_KEY = "sound_vibration";
	public static final String TEXT_TO_SPEECH_KEY = "text_to_speech";
	public static final String TRACK_LOCATION_KEY = "track_location";
	public static final String SHOW_LANDMARKS_USING_KEY = "show_landmarks_using";
	public static final String PROXIMITY_RADIUS_KEY = "proximity_radius";
	public static final String FORCE_LOCATION_TO_HOME_LAT_KEY = "force_location_to_home_lat";
	public static final String FORCE_LOCATION_TO_HOME_LON_KEY = "force_location_to_home_lon";
	public static final String ABOUT_KEY = "about";

	private CheckBoxPreference mDebug;
	private ListPreference mLanguage;
	private EditTextPreference mHostname;
	private CheckBoxPreference mOfflineMode;
	private PreferenceScreen mViewCache;
	private ListPreference mStyle;
	private CheckBoxPreference mSoundVibration;
	private CheckBoxPreference mTextToSpeech;
	private CheckBoxPreference mTrackLocation;
	private ListPreference mShowLandmarksUsing;
	private ListPreference mProximityRadius;
	private PreferenceScreen mAbout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.app_settings);

		mDebug = (CheckBoxPreference) findPreference(DEBUG_KEY);
		mLanguage = (ListPreference) findPreference(LANGUAGE_KEY);
		mHostname = (EditTextPreference) findPreference(HOSTNAME_KEY);
		mOfflineMode = (CheckBoxPreference) findPreference(OFFLINE_MODE_KEY);
		mViewCache = (PreferenceScreen) findPreference(CACHE_KEY);
		mViewCache
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent();
						intent.setClass(AppSettings.this, CacheList.class);
						startActivity(intent);
						return true;
					}
				});
		mStyle = (ListPreference) findPreference(STYLE_KEY);
		mStyle.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				final ProgressDialog dialog = new ProgressDialog(
						AppSettings.this);
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setMessage("Clearing map cache...");
				dialog.setCancelable(false);
				new Thread() {
					public void run() {
						Utils.clearMapCache(new Utils.OnDeletionProgressListener() {
							@Override
							public void deleted(final int deletedFileCount,
									final int totalFileCount) {
								getListView().post(new Runnable() {
									@Override
									public void run() {
										dialog.setProgress(deletedFileCount
												* 100 / totalFileCount);
										if (deletedFileCount >= totalFileCount) {
											dialog.dismiss();
										}
									}
								});
							}
						});
					}
				}.start();
				return true;
			}
		});
		mSoundVibration = (CheckBoxPreference) findPreference(SOUND_VIBRATION_KEY);
		mTextToSpeech = (CheckBoxPreference) findPreference(TEXT_TO_SPEECH_KEY);
		mTrackLocation = (CheckBoxPreference) findPreference(TRACK_LOCATION_KEY);
		mShowLandmarksUsing = (ListPreference) findPreference(SHOW_LANDMARKS_USING_KEY);
		mProximityRadius = (ListPreference) findPreference(PROXIMITY_RADIUS_KEY);
		mAbout = (PreferenceScreen) findPreference(ABOUT_KEY);
		mAbout.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(AppSettings.this, About.class);
				startActivity(intent);
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		((Hilos) getApplication()).loadSettings();
		mDebug.setChecked(Hilos.isDebug());
		mLanguage.setValue(Hilos.getLanguage());
		mHostname.setText(Hilos.getHostname());
		mOfflineMode.setChecked(Hilos.isOfflineMode());
		mStyle.setValue(Hilos.getStyle());
		mSoundVibration.setChecked(Hilos.isSoundVibration());
		mTextToSpeech.setChecked(Hilos.isTextToSpeech());
		mTrackLocation.setChecked(Hilos.isTrackLocation());
		mShowLandmarksUsing.setValue(Hilos.getShowLandmarksUsing());
		mProximityRadius.setValue("" + Hilos.getProximityRadius());
		super.onResume();
	}

	@Override
	protected void onPause() {
		Hilos.setDebug(mDebug.isChecked());
		Hilos.setLanguage(mLanguage.getValue());
		Hilos.setHostname(mHostname.getText());
		Hilos.setOfflineMode(mOfflineMode.isChecked());
		Hilos.setStyle(mStyle.getValue());
		Hilos.setSoundVibration(mSoundVibration.isChecked());
		Hilos.setTextToSpeech(mTextToSpeech.isChecked());
		Hilos.setTrackLocation(mTrackLocation.isChecked());
		Hilos.setShowLandmarksUsing(mShowLandmarksUsing.getValue());
		Hilos.setProximityRadius(Integer.parseInt(mProximityRadius.getValue()));
		((Hilos) getApplication()).saveSettings();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, Splash.class));
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		setResult(RESULT_OK, null);
		super.onDestroy();
	}
}

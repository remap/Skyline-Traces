package edu.ucla.tft.hilos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.osmdroid.util.GeoPoint;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 * Hilos application class. Stores initialization settings as static variables,
 * which can be accessed by other parts of the application to adjust their
 * behavior.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Hilos extends Application {
	// Constants to identify different languages
	public static final String LANGUAGE_0 = "en";
	public static final String LANGUAGE_1 = "es";
	public static final String LANGUAGE_2 = "zh";
	private static final String[] LANGUAGES = { LANGUAGE_0, LANGUAGE_1,
			LANGUAGE_2 };
	// Constants to identify different styles
	public static final String STYLE_0 = "style_0";
	public static final String STYLE_1 = "style_1";
	public static final String STYLE_2 = "style_2";
	private static final String[] STYLES = { STYLE_0, STYLE_1, STYLE_2 };
	// Constants to identify how to show media items related to a landmark
	public static final String SHOW_LANDMARKS_USING_ASSOCIATION = "show_landmarks_using_association";
	public static final String SHOW_LANDMARKS_USING_PROXIMITY = "show_landmarks_using_proximity";
	private static final String[] SHOW_LANDMARKS_USING = {
			SHOW_LANDMARKS_USING_ASSOCIATION, SHOW_LANDMARKS_USING_PROXIMITY };
	// Constants to identify different proximity radii
	public static final int PROXIMITY_RADIUS_100 = 100;
	public static final int PROXIMITY_RADIUS_250 = 250;
	public static final int PROXIMITY_RADIUS_500 = 500;
	public static final int PROXIMITY_RADIUS_1000 = 1000;
	public static final int PROXIMITY_RADIUS_0 = 0;

	private static Map<String, StyleInfo> sStyleInfoMap;

	public static Drawable sBackgroundTextDrawable;
	public static Drawable sBackgroundTextSelectedDrawable;

	private static boolean sDebug = true;

	public static boolean isDebug() {
		return sDebug;
	}

	public static void setDebug(boolean debug) {
		sDebug = debug;
	}

	private static String sLanguage = LANGUAGE_0;

	public static String getLanguage() {
		return sLanguage;
	}

	public static void setLanguage(String language) {
		sLanguage = language;
	}

	public static int getLanguageIndex() {
		return Arrays.asList(LANGUAGES).indexOf(getLanguage());
	}

	public static void setLanguageIndex(int index) {
		setLanguage(LANGUAGES[index]);
	}

	private static String sHostname = "http://la.remap.ucla.edu:8080/hilos";

	public static String getHostname() {
		return sHostname;
	}

	public static void setHostname(String hostname) {
		sHostname = hostname;
	}

	private static boolean sOfflineMode = false;

	public static boolean isOfflineMode() {
		return sOfflineMode;
	}

	public static void setOfflineMode(boolean offlineMode) {
		sOfflineMode = offlineMode;
	}

	// String identifier for the style
	private static String sStyle;

	public static String getStyle() {
		return sStyle;
	}

	public static void setStyle(String style) {
		sStyle = style;
	}

	public static int getStyleIndex() {
		return Arrays.asList(STYLES).indexOf(getStyle());
	}

	public static void setStyleIndex(int index) {
		setStyle(STYLES[index]);
	}

	// String identifier for the style
	private static String sShowLandmarksUsing;
	private static int sShowLandmarksUsingIndex;

	public static String getShowLandmarksUsing() {
		return sShowLandmarksUsing;
	}

	public static void setShowLandmarksUsing(String showLandmarksUsing) {
		sShowLandmarksUsing = showLandmarksUsing;
		sShowLandmarksUsingIndex = Arrays.asList(SHOW_LANDMARKS_USING).indexOf(
				getShowLandmarksUsing());
	}

	public static int getShowLandmarksUsingIndex() {
		return sShowLandmarksUsingIndex;
	}

	public static void setShowLandmarksUsingIndex(int index) {
		setShowLandmarksUsing(STYLES[index]);
	}

	private static boolean sTrackLocation;

	public static boolean isTrackLocation() {
		return sTrackLocation;
	}

	public static void setTrackLocation(boolean trackLocation) {
		sTrackLocation = trackLocation;
	}

	// Proximity radius around current location within which landmarks are shown
	private static int sProximityRadius;

	public static int getProximityRadius() {
		return sProximityRadius;
	}

	public static void setProximityRadius(int proximityRadius) {
		sProximityRadius = proximityRadius;
	}

	private static int sUID = 1;

	public static int getUID() {
		return sUID;
	}

	public static void setUID(int uid) {
		sUID = uid;
	}

	public static void logout() {
		sUID = 0;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Resources res = getResources();
		if (sStyleInfoMap == null) {
			sStyleInfoMap = new HashMap<String, StyleInfo>();
			String[] styleNames = res.getStringArray(R.array.style_names);
			sStyleInfoMap.put(STYLE_0, new StyleInfo(0, styleNames[0],
					R.style.DarkTheme, Color.WHITE, Color.BLACK, true));
			sStyleInfoMap.put(STYLE_1, new StyleInfo(1, styleNames[1],
					R.style.LightTheme, Color.BLACK, Color.WHITE, false));
			sStyleInfoMap.put(STYLE_2, new StyleInfo(2, styleNames[2],
					R.style.LightTheme, Color.BLACK, Color.WHITE, false));
		}
		sBackgroundTextDrawable = res.getDrawable(R.drawable.bg_highlight_text);
		sBackgroundTextSelectedDrawable = res
				.getDrawable(R.drawable.bg_highlight_text_selected);
		loadSettings();
	}

	public static StyleInfo getCurrStyleInfo() {
		return sStyleInfoMap.get(getStyle());
	}

	public static int getStylesCount() {
		return sStyleInfoMap.size();
	}

	public static StyleInfo getStyleInfo(int index) {
		for (StyleInfo si : sStyleInfoMap.values()) {
			if (si.index == index) {
				return si;
			}
		}
		return null;
	}

	private static int sForceLocationToHomeLatE6;
	private static int sForceLocationToHomeLonE6;

	public static GeoPoint getForceLocationToHome() {
		return new GeoPoint(sForceLocationToHomeLatE6,
				sForceLocationToHomeLonE6);
	}

	public static void setForceLocationToHome(GeoPoint geoPoint) {
		sForceLocationToHomeLatE6 = geoPoint.getLatitudeE6();
		sForceLocationToHomeLonE6 = geoPoint.getLongitudeE6();
	}

	private static boolean sSoundVibration;

	public static boolean isSoundVibration() {
		return sSoundVibration;
	}

	public static void setSoundVibration(boolean sv) {
		sSoundVibration = sv;
	}

	private static boolean sTextToSpeech;

	public static boolean isTextToSpeech() {
		return sTextToSpeech;
	}

	public static void setTextToSpeech(boolean tts) {
		sTextToSpeech = tts;
	}

	/**
	 * Load settings from a shared preferences file.
	 */
	public void loadSettings() {
		SharedPreferences settings = getSharedPreferences("preferences", 0);
		setDebug(settings.getBoolean(AppSettings.DEBUG_KEY, false));
		setLanguage(settings.getString(AppSettings.LANGUAGE_KEY, LANGUAGE_0));
		setOfflineMode(settings.getBoolean(AppSettings.OFFLINE_MODE_KEY, false));
		setHostname(settings.getString(AppSettings.HOSTNAME_KEY,
				"http://la.remap.ucla.edu:8080/hilos"));
		setStyle(settings.getString(AppSettings.STYLE_KEY, STYLE_1));
		setTrackLocation(settings.getBoolean(AppSettings.TRACK_LOCATION_KEY,
				true));
		setShowLandmarksUsing(settings.getString(
				AppSettings.SHOW_LANDMARKS_USING_KEY,
				SHOW_LANDMARKS_USING_ASSOCIATION));
		setProximityRadius(settings.getInt(AppSettings.PROXIMITY_RADIUS_KEY,
				PROXIMITY_RADIUS_100));
		int lat = settings
				.getInt(AppSettings.FORCE_LOCATION_TO_HOME_LAT_KEY, 0);
		int lon = settings
				.getInt(AppSettings.FORCE_LOCATION_TO_HOME_LON_KEY, 0);
		setForceLocationToHome(new GeoPoint(lat, lon));
		setTextToSpeech(settings.getBoolean(AppSettings.TEXT_TO_SPEECH_KEY,
				false));
		setSoundVibration(settings.getBoolean(AppSettings.SOUND_VIBRATION_KEY,
				true));
	}

	public void saveSettings() {
		SharedPreferences settings = getSharedPreferences("preferences", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(AppSettings.DEBUG_KEY, isDebug());
		editor.putString(AppSettings.LANGUAGE_KEY, getLanguage());
		editor.putString(AppSettings.HOSTNAME_KEY, getHostname());
		editor.putBoolean(AppSettings.OFFLINE_MODE_KEY, isOfflineMode());
		editor.putString(AppSettings.STYLE_KEY, getStyle());
		editor.putBoolean(AppSettings.TRACK_LOCATION_KEY, isTrackLocation());
		editor.putString(AppSettings.SHOW_LANDMARKS_USING_KEY,
				getShowLandmarksUsing());
		editor.putInt(AppSettings.PROXIMITY_RADIUS_KEY, getProximityRadius());
		editor.putInt(AppSettings.FORCE_LOCATION_TO_HOME_LAT_KEY,
				getForceLocationToHome().getLatitudeE6());
		editor.putInt(AppSettings.FORCE_LOCATION_TO_HOME_LON_KEY,
				getForceLocationToHome().getLongitudeE6());
		editor.putBoolean(AppSettings.SOUND_VIBRATION_KEY, isSoundVibration());
		editor.putBoolean(AppSettings.TEXT_TO_SPEECH_KEY, isTextToSpeech());
		editor.commit();
	}
}

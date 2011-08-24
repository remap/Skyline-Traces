package edu.ucla.tft.hilosupdater;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * Activity for displaying and modifying application settings. Each setting has
 * a corresponding static variable in the Updater class. Lifecycle methods
 * (onResume, onPause) are used to read and write to these variables.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class AppSettings extends PreferenceActivity {
	public static final String DEBUG_KEY = "debug";
	public static final String HOSTNAME_KEY = "hostname";
	public static final String ABOUT_KEY = "about";

	private CheckBoxPreference mDebug;
	private EditTextPreference mHostname;
	private PreferenceScreen mAbout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.app_settings);

		mDebug = (CheckBoxPreference) findPreference(DEBUG_KEY);
		mHostname = (EditTextPreference) findPreference(HOSTNAME_KEY);
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
		mDebug.setChecked(Updater.isDebug());
		mHostname.setText(Updater.getHostname());
		super.onResume();
	}

	@Override
	protected void onPause() {
		Updater.setDebug(mDebug.isChecked());
		Updater.setHostname(mHostname.getText());
		super.onPause();
	}

	@Override
	protected void onStop() {
		setResult(RESULT_OK, null);
		super.onStop();
	}
}

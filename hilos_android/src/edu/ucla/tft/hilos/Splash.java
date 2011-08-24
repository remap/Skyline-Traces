package edu.ucla.tft.hilos;

import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.ucla.tft.hilos.StyleInfo.SoundId;
import edu.ucla.tft.hilos.maplet.MapDisplay;

/**
 * Activity shown when the application is launched. When the activity is run for
 * the first time, it downloads all application user interface and related
 * assets and presents a splash screen. Also provides access to application
 * customization.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Splash extends InitializationActivity {
	public static final int DIALOG_LANGUAGE_ID = 0x0000001;
	public static final int DIALOG_STYLE_ID = 0x00000002;

	private Button mSettings;
	private Button mChangeLanguage;
	private Button mChangeStyle;
	private Button mLoginJoin;
	private SplashView mSplashView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected String getBackgroundSoundPath() {
		return Hilos.getCurrStyleInfo().getSoundFilePath(SoundId.splash);
	}

	protected int getBackgroundSoundBehavior() {
		return BGSOUND_START;
	}

	private String mInitializationError;
	private String mInitializationWarnings;

	@Override
	protected boolean initialize() {
		// wait so that content view is inflated
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mInitializationError = null;
		for (int i = 0; i < Hilos.getStylesCount(); i++) {
			StyleInfo styleInfo = Hilos.getStyleInfo(i);
			if (!styleInfo.isAvailable() && !styleInfo.load()) {
				mInitializationError = "Unable to load style: "
						+ styleInfo.name;
				return false;
			}
		}
		mInitializationWarnings = null;
		if (Hilos.isOfflineMode()) {
			int nWarnings = 0;
			for (Resource res : Network.getResourceList(null, false)) {
				InputStream in;
				if ((in = Network.getResourceInputStream(res.id,
						res.resourceType, false)) == null) {
					mInitializationWarnings = (nWarnings++ > 0 ? mInitializationWarnings
							+ "\n"
							: "")
							+ "Could not load: " + res.title;
				} else {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return true;
	}

	@Override
	protected void postInitialize(boolean initialized) {
		if (!initialized && mInitializationError != null) {
			showErrorDialog(mInitializationError);
			return;
		} else if (mInitializationWarnings != null) {
			showWarningsDialog(mInitializationWarnings);
		} else {
			initializeView();
		}
	}

	private void showErrorDialog(final String error) {
		mContentView.post(new Runnable() {
			@Override
			public void run() {
				DialogInterface.OnClickListener mDialogAction = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_NEGATIVE:
							dialog.dismiss();
							break;
						case DialogInterface.BUTTON_NEUTRAL:
							showSettings();
							break;
						}
					}
				};
				new AlertDialog.Builder(Splash.this).setMessage(error)
						.setNegativeButton("Dismiss", mDialogAction)
						.setNeutralButton("Settings", mDialogAction)
						.setTitle("Error").create().show();
			}
		});
	}

	private void showWarningsDialog(final String warnings) {
		mContentView.post(new Runnable() {
			@Override
			public void run() {
				DialogInterface.OnClickListener mDialogAction = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						initializeView();
					}
				};
				new AlertDialog.Builder(Splash.this).setMessage(warnings)
						.setNegativeButton("Dismiss", mDialogAction)
						.setTitle("Warning").create().show();
			}
		});
	}

	private void showSettings() {
		View view = LayoutInflater.from(this).inflate(R.layout.admin_login,
				null);
		final EditText passwordText = (EditText) view
				.findViewById(R.id.admin_password);
		DialogInterface.OnClickListener dialogAction = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					String password = passwordText.getText().toString();
					if ("stadmin".equals(password)) {
						Intent intent = new Intent(Splash.this,
								AppSettings.class);
						startActivity(intent);
						Splash.this.finish();
					} else {
						Toast.makeText(Splash.this, R.string.invalid_password,
								Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		};
		Dialog d = new AlertDialog.Builder(this).setTitle(R.string.settings)
				.setView(view).setPositiveButton(R.string.login, dialogAction)
				.setNegativeButton(R.string.cancel, dialogAction).create();
		d.show();
	}

	private void initializeView() {
		mContentView.post(new Runnable() {
			public void run() {
				mSplashView = (SplashView) LayoutInflater.from(Splash.this)
						.inflate(R.layout.splash, null);
				StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
				mSettings = (Button) mSplashView.findViewById(R.id.settings);
				mSettings.setBackgroundDrawable(currStyleInfo
						.getStateListDrawable(StyleInfo.StyleStateListId.btn_settings));
				mSettings.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						showSettings();
					}
				});
				mChangeLanguage = (Button) mSplashView
						.findViewById(R.id.change_language);
				mChangeLanguage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_LANGUAGE_ID);
					}
				});
				mChangeStyle = (Button) mSplashView
						.findViewById(R.id.change_style);
				mChangeStyle.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_STYLE_ID);
					}
				});
				mLoginJoin = (Button) mSplashView.findViewById(R.id.login_join);
				mLoginJoin.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(Splash.this, Login.class));
					}
				});
				mContentView.addView(mSplashView);
				mContentView.invalidate();
				Network.init();
			}
		});
	}

	@Override
	public void onDestroy() {
		if (mSplashView != null) {
			mSplashView.releaseResources();
		}
		super.onDestroy();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		if (mSplashView.doContinue(x, y, event.getAction())) {
			Hilos.setUID(0);
			startActivity(new Intent(Splash.this, MapDisplay.class));
			finish();
			return true;
		}
		return super.onTouchEvent(event);
	}

	private void doWelcome() {
		mSpeechEngine.speakQueued("Welcome to Hilos.");
		mSpeechEngine.speakQueued("This is an interactive application.");
		mSpeechEngine
				.speakQueued("To begin, touch the logo in the center of the screen.");
		mSpeechEngine
				.speakQueued("To log in or register, click the button on the lower right.");
		mSpeechEngine
				.speakQueued("To change the language, click on the button on the top right.");
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DIALOG_LANGUAGE_ID: {
			Resources res = getResources();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(res.getString(R.string.language));
			String[] choices = res.getStringArray(R.array.language_names);
			builder.setSingleChoiceItems(choices, Hilos.getLanguageIndex(),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Hilos.setLanguageIndex(which);
							((Hilos) getApplication()).saveSettings();
							startActivity(new Intent(Splash.this, Splash.class));
							Splash.this.finish();
						}
					});
			builder.setCancelable(true);
			return builder.create();
		}
		case DIALOG_STYLE_ID: {
			return StyleChooserDialog.create(this,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Hilos.setStyleIndex(StyleChooserDialog
									.getCurrIndex());
							((Hilos) getApplication()).saveSettings();
							startActivity(new Intent(Splash.this, Splash.class));
							Splash.this.finish();
						}
					});
		}
		}
		return super.onCreateDialog(id, args);
	}
}
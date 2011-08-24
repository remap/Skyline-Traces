package edu.ucla.tft.hilosupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.ucla.tft.hilosupdater.Network.Version;

/**
 * Activity to download and install an updated version of the application from a
 * server. Also contains application settings as static variables which can be
 * used to modify the behavior of the application.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Updater extends Activity {
	private static boolean sDebug;

	public static boolean isDebug() {
		return sDebug;
	}

	public static void setDebug(boolean debug) {
		sDebug = debug;
	}

	private static String sHostname;

	public static String getHostname() {
		return sHostname;
	}

	public static void setHostname(String hostname) {
		sHostname = hostname;
	}

	private Thread taskThread;
	private CheckBox mUninstall;
	private Button mUpdateAndInstall;
	private Button mSettings;
	private View mProgressPanel;
	private View mUpdaterView;
	private TextView mProgressText;
	private ProgressBar mProgress;
	private Button mCancelDownload;
	private boolean mDownloadCanceled;
	private File mUpdateFile;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUpdaterView = LayoutInflater.from(this).inflate(R.layout.main, null);
		setContentView(mUpdaterView);

		mUninstall = (CheckBox) findViewById(R.id.uninstall);
		mUpdateAndInstall = (Button) findViewById(R.id.updateAndInstall);
		mUpdateAndInstall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgress.setProgress(0);
				doLongTask(new LongTask() {
					private String error = null;

					@Override
					public void run() {
						setDownloadCanceled(false);
						mCancelDownload.setEnabled(true);
						try {
							final Version version = Network.getLatestVersion();
							if (version == null) {
								error = "Unable to establish connection.";
								return;
							}
							final InputStream in = Network
									.getUpdateInputStream();
							File tmpDir = new File(Environment
									.getExternalStorageDirectory()
									+ "/hilos/tmp");
							if (!tmpDir.exists()) {
								tmpDir.mkdirs();
							}
							mUpdateFile = new File(tmpDir + "/hilos-"
									+ version.latestVersion + ".apk");
							final FileOutputStream out = new FileOutputStream(
									mUpdateFile);
							Thread downloadThread = new Thread() {
								private long count;

								public void run() {
									try {
										byte[] buf = new byte[4096];
										int n;
										count = 0;
										while (!isDownloadCanceled()
												&& (n = in.read(buf)) > 0) {
											out.write(buf, 0, n);
											count += n;
											mUpdaterView.post(new Runnable() {
												public void run() {
													int percent = (int) (count * 100 / version.fileSize);
													String s = "V"
															+ version.latestVersion
															+ " (" + percent
															+ "%)";
													mProgressText.setText(s);
													mProgress
															.setProgress(percent);
												}
											});
										}
									} catch (IOException e) {
										if (!isInterrupted()) {
											error = e.getMessage();
										}
									}
								}
							};
							downloadThread.start();
							do {
								Thread.sleep(1000);
							} while (!isDownloadCanceled()
									&& downloadThread.isAlive());
							if (downloadThread.isAlive()) {
								downloadThread.interrupt();
							} else {
								out.close();
								in.close();
							}
							if (isDownloadCanceled()) {
								error = "Download canceled.";
								return;
							}
							if (mUninstall.isChecked()) {
								uninstall();
							} else {
								install(mUpdateFile);
							}
						} catch (Exception e) {
							e.printStackTrace();
							error = e.getMessage();
						}
					}

					@Override
					public void done() {
						if (error != null) {
							mUpdaterView.post(new Runnable() {
								public void run() {
									AlertDialog d = new AlertDialog.Builder(
											Updater.this)
											.setMessage(error)
											.setNeutralButton(
													"Okay",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															dialog.dismiss();
														}
													}).setTitle("Error")
											.create();
									d.show();
								}
							});
						}
					}
				});
			}
		});
		mSettings = (Button) findViewById(R.id.settings);
		mSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Updater.this, AppSettings.class);
				startActivity(intent);
			}
		});
		mProgressPanel = findViewById(R.id.progressPanel);
		mProgressText = (TextView) findViewById(R.id.progressText);
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mCancelDownload = (Button) findViewById(R.id.cancel);
		mCancelDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCancelDownload.setEnabled(false);
				setDownloadCanceled(true);
			}
		});
		loadSettings();
	}

	private boolean mUninstalled;

	private void uninstall() {
		Uri packageURI = Uri.parse("package:edu.ucla.tft.hilos");
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		startActivity(uninstallIntent);
		mUninstalled = true;
	}

	private void install(File tmpFile) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(tmpFile),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			if (mUninstalled) {
				mUninstalled = false;
				install(mUpdateFile);
			} else {
				saveSettings();
			}
		}
	}

	public void loadSettings() {
		SharedPreferences settings = getSharedPreferences("prefs", 0);
		Updater.setDebug(settings.getBoolean(AppSettings.DEBUG_KEY, false));
		Updater.setHostname(settings.getString(AppSettings.HOSTNAME_KEY,
				"http://la.remap.ucla.edu:8080/hilos"));
	}

	public void saveSettings() {
		SharedPreferences settings = getSharedPreferences("prefs", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(AppSettings.DEBUG_KEY, Updater.isDebug());
		editor.putString(AppSettings.HOSTNAME_KEY, Updater.getHostname());
		editor.commit();
	}

	private boolean isDownloadCanceled() {
		return mDownloadCanceled;
	}

	private void setDownloadCanceled(boolean b) {
		mDownloadCanceled = b;
	}

	public boolean doLongTask(final LongTask task) {
		setRequestedOrientation(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
				: ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mUninstall.setEnabled(false);
		mUpdateAndInstall.setEnabled(false);
		mSettings.setEnabled(false);
		mProgressPanel.setVisibility(View.VISIBLE);
		if (taskThread != null) {
			return false;
		}
		taskThread = new Thread() {
			@Override
			public void run() {
				task.run();
				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				mUpdaterView.post(new Runnable() {
					@Override
					public void run() {
						mUninstall.setEnabled(true);
						mUpdateAndInstall.setEnabled(true);
						mSettings.setEnabled(true);
						mProgressPanel.setVisibility(View.INVISIBLE);
					}
				});
				task.done();
				taskThread = null;
			}
		};
		taskThread.start();
		return true;
	}
}
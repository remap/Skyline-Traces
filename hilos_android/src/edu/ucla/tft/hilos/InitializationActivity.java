package edu.ucla.tft.hilos;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * An activity which has a long initialization step can inherit from this class
 * to show a progress dialog which the initialization takes place.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public abstract class InitializationActivity extends BaseActivity {
	protected ViewGroup mContentView;
	protected boolean mInitialized;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContentView = (ViewGroup) LayoutInflater.from(this).inflate(
				R.layout.empty, null);
		setContentView(mContentView);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mInitialized) {
			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setCancelable(false);
			dialog.setMessage("Initialization in progress...");
			dialog.setTitle("Please Wait");
			dialog.setIndeterminate(true);
			dialog.show();
			new Thread() {
				@Override
				public void run() {
					mInitialized = initialize();
					dialog.dismiss();
					postInitialize(mInitialized);
				}
			}.start();
		}
	}

	protected abstract boolean initialize();

	protected abstract void postInitialize(boolean initialized);
}
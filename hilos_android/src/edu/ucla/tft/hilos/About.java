package edu.ucla.tft.hilos;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

/**
 * Activity displaying an 'about' page containing copyright and other
 * information. The source page is 'about.html' under the 'assets' directory.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 */
public class About extends Activity {
	private static final String URL_ABOUT = "file:///android_asset/about.html";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		WebView mWebView = (WebView) LayoutInflater.from(this).inflate(
				R.layout.about, null);
		mWebView.loadUrl(URL_ABOUT);
		setContentView(mWebView);
	}
}
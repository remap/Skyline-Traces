package edu.ucla.tft.hilos;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

/**
 * Activity displaying a 'help' page containing instructions on how to use the
 * application.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 */
public class Help extends Activity {
	private static final String URL_HELP = "file:///android_asset/help.html";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		WebView mWebView = (WebView) LayoutInflater.from(this).inflate(
				R.layout.help, null);
		mWebView.loadUrl(URL_HELP);
		setContentView(mWebView);
	}
}
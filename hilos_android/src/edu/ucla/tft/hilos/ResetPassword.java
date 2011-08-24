package edu.ucla.tft.hilos;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.ucla.tft.hilos.Network.ResetPasswordTask;

/**
 * Activity used to reset a password.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class ResetPassword extends Activity {
	private EditText mEmail;
	private Button mSendNewPassword;

	private View mResetPasswordView;

	private DialogInterface.OnClickListener mDismiss = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		StyleInfo styleInfo = Hilos.getCurrStyleInfo();
		setTheme(styleInfo.systemTheme);

		mResetPasswordView = (View) LayoutInflater.from(this).inflate(
				R.layout.reset_password, null);
		mEmail = (EditText) mResetPasswordView.findViewById(R.id.email);
		mEmail.setText("singh.jasleen@gmail.com");
		mSendNewPassword = (Button) mResetPasswordView
				.findViewById(R.id.send_new_password);
		mSendNewPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mResetPasswordView.post(new Runnable() {
					@Override
					public void run() {
						final String email = mEmail.getText().toString().trim();
						final String lang = Locale.getDefault().getLanguage();
						if (email.length() <= 0) {
							new AlertDialog.Builder(ResetPassword.this)
									.setMessage(
											"Please fill in all the fields.")
									.setNeutralButton("Okay", mDismiss)
									.setTitle("Error").create().show();
							return;
						}
						if (!EmailValidator.getInstance().isValid(email)) {
							new AlertDialog.Builder(ResetPassword.this)
									.setMessage(
											"Please enter a valid email address.")
									.setNeutralButton("Okay", mDismiss)
									.setTitle("Error").create().show();
							return;
						}
						Network.execute(new ResetPasswordTask(email, lang) {
							@Override
							public void done() {
								Intent intent = new Intent();
								intent.putExtra("email", email);
								intent.putExtra("lang", lang);
								setResult(RESULT_OK, intent);
								finish();
							}

							@Override
							public void error(String e) {
								new AlertDialog.Builder(ResetPassword.this)
										.setMessage(e)
										.setNeutralButton("Okay", mDismiss)
										.setTitle("Error").create().show();
							}
						});
					}
				});
			}
		});
		setContentView(mResetPasswordView);
	}
}
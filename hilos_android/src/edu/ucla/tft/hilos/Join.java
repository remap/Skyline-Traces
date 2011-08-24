package edu.ucla.tft.hilos;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.ucla.tft.hilos.Network.JoinTask;

/**
 * Activity to register a new user.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class Join extends BaseActivity {
	private EditText mEmail;
	private EditText mPassword;
	private EditText mConfirmPassword;
	private EditText mName;
	private Button mJoin;

	private View mJoinView;

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

		mJoinView = LayoutInflater.from(this).inflate(R.layout.join, null);
		mEmail = (EditText) mJoinView.findViewById(R.id.email);
		mPassword = (EditText) mJoinView.findViewById(R.id.password);
		mConfirmPassword = (EditText) mJoinView
				.findViewById(R.id.confirm_password);
		mName = (EditText) mJoinView.findViewById(R.id.name);
		mJoin = (Button) mJoinView.findViewById(R.id.join);
		mJoin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mJoinView.post(new Runnable() {
					@Override
					public void run() {
						Resources res = getResources();
						final String email = mEmail.getText().toString().trim();
						final String password = mPassword.getText().toString()
								.trim();
						final String confirmPassword = mConfirmPassword
								.getText().toString().trim();
						final String name = mName.getText().toString().trim();
						final String lang = Locale.getDefault().getLanguage();
						if (email.length() <= 0 || password.length() <= 0
								|| confirmPassword.length() <= 0
								|| name.length() <= 0) {
							new AlertDialog.Builder(Join.this)
									.setMessage(
											res.getString(R.string.fill_in_all_fields))
									.setNeutralButton(
											res.getString(R.string.ok),
											mDismiss).setTitle(R.string.error)
									.create().show();
							return;
						}
						if (!EmailValidator.getInstance().isValid(email)) {
							new AlertDialog.Builder(Join.this)
									.setMessage(
											res.getString(R.string.enter_valid_email))
									.setNeutralButton(
											res.getString(R.string.ok),
											mDismiss).setTitle(R.string.error)
									.create().show();
							return;
						}
						if (!password.equals(confirmPassword)) {
							new AlertDialog.Builder(Join.this)
									.setMessage(
											res.getString(R.string.passwords_do_not_match))
									.setNeutralButton(
											res.getString(R.string.ok),
											mDismiss)
									.setTitle(res.getString(R.string.error))
									.create().show();
							return;
						}
						Network.execute(new JoinTask(email, password, name,
								lang) {
							@Override
							public void done() {
								Intent intent = new Intent();
								intent.putExtra("email", email);
								intent.putExtra("password", password);
								intent.putExtra("name", name);
								intent.putExtra("lang", lang);
								setResult(RESULT_OK, intent);
								finish();
							}

							@Override
							public void error(String e) {
								new AlertDialog.Builder(Join.this)
										.setMessage(e)
										.setNeutralButton("Okay", mDismiss)
										.setTitle("Error").create().show();
							}
						});
					}
				});
			}
		});
		setContentView(mJoinView);
	}
}
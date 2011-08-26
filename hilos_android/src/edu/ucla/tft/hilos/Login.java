package edu.ucla.tft.hilos;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.ucla.tft.hilos.Network.LoginTask;

/**
 * Activity to display a login screen at the start of the application. Also
 * links to a @link{Join} activity which can be used to register a new user.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Login extends BaseActivity {
	public static final int RESET_PASSWORD_REQUEST = 0x00000001;
	public static final int JOIN_ACTIVITY_REQUEST = 0x00000002;
	public static final int DIALOG_LANGUAGE_ID = 0x00001001;

	private TextView mMessage;
	private EditText mEmail;
	private EditText mPassword;
	private TextView mForgotPassword;
	private Button mLogin;
	private Button mJoin;
	private LoginView mLoginView;

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

		mLoginView = (LoginView) LayoutInflater.from(this).inflate(
				R.layout.login, null);
		mMessage = (TextView) mLoginView.findViewById(R.id.message);
		mEmail = (EditText) mLoginView.findViewById(R.id.email);
		mPassword = (EditText) mLoginView.findViewById(R.id.password);
		mForgotPassword = (TextView) mLoginView
				.findViewById(R.id.forgot_password);
		mForgotPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Login.this, ResetPassword.class);
				startActivityForResult(intent, RESET_PASSWORD_REQUEST);
			}
		});
		mLogin = (Button) mLoginView.findViewById(R.id.login);
		mLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLoginView.post(new Runnable() {
					@Override
					public void run() {
						String email = mEmail.getText().toString().trim();
						String password = mPassword.getText().toString().trim();
						String lang = Locale.getDefault().getLanguage();
						Resources res = getResources();
						if (email.length() <= 0 || password.length() <= 0) {
							new AlertDialog.Builder(Login.this)
									.setMessage(
											res.getString(R.string.fill_in_all_fields))
									.setNeutralButton(
											res.getString(R.string.ok),
											mDismiss)
									.setTitle(res.getString(R.string.error))
									.create().show();
							return;
						}
						if (!EmailValidator.getInstance().isValid(email)) {
							new AlertDialog.Builder(Login.this)
									.setMessage(
											res.getString(R.string.enter_valid_email))
									.setNeutralButton(
											res.getString(R.string.ok),
											mDismiss)
									.setTitle(res.getString(R.string.error))
									.create().show();
							return;
						}
						Network.execute(new LoginTask(email, password, lang) {
							@Override
							public void done(int uid) {
								Hilos.setUID(uid);
								Login.this.finish();
							}

							@Override
							public void error(String e) {
								new AlertDialog.Builder(Login.this)
										.setMessage(e)
										.setNeutralButton("Okay", mDismiss)
										.setTitle("Error").create().show();
							}
						});
					}
				});
			}
		});
		mJoin = (Button) mLoginView.findViewById(R.id.join);
		mJoin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Login.this, Join.class);
				startActivityForResult(intent, JOIN_ACTIVITY_REQUEST);
			}
		});
		setContentView(mLoginView);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mMessage.setText("");
		switch (requestCode) {
		case RESET_PASSWORD_REQUEST:
			if (resultCode == RESULT_OK) {
				Resources res = getResources();
				mMessage.setText(res.getString(R.string.your_password_reset));
				mEmail.setText(data.getStringExtra("email"));
				mPassword.setText("");
			}
			break;
		case JOIN_ACTIVITY_REQUEST:
			if (resultCode == RESULT_OK) {
				Resources res = getResources();
				mMessage.setText(res
						.getString(R.string.verify_email_to_continue));
				mEmail.setText(data.getStringExtra("email"));
				mPassword.setText(data.getStringExtra("password"));
			}
			break;
		}
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
							startActivity(new Intent(Login.this, Login.class));
							Login.this.finish();
						}
					});
			builder.setCancelable(true);
			return builder.create();
		}
		}
		return super.onCreateDialog(id, args);
	}

}
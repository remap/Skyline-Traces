package edu.ucla.tft.hilos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import edu.ucla.tft.hilos.StyleInfo.SoundId;

/**
 * Abstract super class for applications activitie, providing common functions
 * such as audio and haptic feedback, text to speech etc.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public abstract class BaseActivity extends Activity implements
		SpeechEngine.Listener {
	public static final int EC_TICK = 0;
	public static final int EC_TOCK = 1;
	public static final int EC_START = 2;
	public static final int EC_FINISH = 3;
	public static final int EC_SELECT = 4;
	public static final int EC_DESELECT = 5;
	public static final int EC_POP = 6;
	public static final int EC_FLIP = 7;

	protected SpeechEngine mSpeechEngine;
	protected Handler mHandler = new Handler();
	protected MediaPlayer mMediaPlayer;
	protected Vibrator mVibrator;
	protected SoundPool mSoundPool;
	protected Map<Integer, Integer> mEarconMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		StyleInfo styleInfo = Hilos.getCurrStyleInfo();
		setTheme(styleInfo.systemTheme);
		Configuration config = new Configuration();
		config.locale = new Locale(Hilos.getLanguage());
		getBaseContext().getResources().updateConfiguration(config, null);
		String bgSoundPath = getBackgroundSoundPath();
		if (bgSoundPath != null) {
			try {
				mMediaPlayer = MediaPlayer.create(this,
						Uri.parse("file://" + bgSoundPath));
				mMediaPlayer.setLooping(true);
			} catch (Exception e) {
				e.printStackTrace();
				mMediaPlayer = null;
			}
		}
		mSpeechEngine = new SpeechEngine(this);
		mSpeechEngine.setListener(this);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		mEarconMap = new HashMap<Integer, Integer>();
		mEarconMap.put(EC_TICK,
				loadEarcon(styleInfo.getSoundFilePath(SoundId.tick_snd)));
		mEarconMap.put(EC_TOCK,
				loadEarcon(styleInfo.getSoundFilePath(SoundId.tock_snd)));
		mEarconMap.put(EC_START,
				loadEarcon(styleInfo.getSoundFilePath(SoundId.start_snd)));
		mEarconMap.put(EC_FINISH,
				loadEarcon(styleInfo.getSoundFilePath(SoundId.finish_snd)));
		mEarconMap.put(EC_SELECT,
				loadEarcon(styleInfo.getSoundFilePath(SoundId.select_snd)));
		mEarconMap.put(EC_DESELECT,
				loadEarcon(styleInfo.getSoundFilePath(SoundId.deselect_snd)));
		mEarconMap.put(EC_POP,
				loadEarcon(styleInfo.getSoundFilePath(SoundId.pop_snd)));
		mEarconMap.put(EC_FLIP,
				loadEarcon(styleInfo.getSoundFilePath(SoundId.flip_snd)));
	}

	protected String getBackgroundSoundPath() {
		return null;
	}

	protected static final int BGSOUND_START = 0x1, BGSOUND_PAUSE = 0x2,
			BGSOUND_STOP = 0x4;

	protected int getBackgroundSoundBehavior() {
		return BGSOUND_START | BGSOUND_PAUSE | BGSOUND_STOP;
	}

	protected int loadEarcon(String path) {
		return mSoundPool.load(path, 1);
	}

	public void playEarcon(int id) {
		if (!Hilos.isSoundVibration()) {
			return;
		}
		mSoundPool.play(mEarconMap.get(id), 1.0f, 1.0f, 1, 0, 1.0f);
	}

	public void vibrate(long milliseconds) {
		if (!Hilos.isSoundVibration()) {
			return;
		}
		mVibrator.vibrate(milliseconds);
	}

	public void vibrate(long[] pattern, int repeat) {
		if (!Hilos.isSoundVibration()) {
			return;
		}
		mVibrator.vibrate(pattern, repeat);
	}

	protected boolean isBackgroundSoundPlaying() {
		return mMediaPlayer != null && mMediaPlayer.isPlaying();
	}

	protected boolean toggleBackgroundSound() {
		if (mMediaPlayer != null) {
			if (!mMediaPlayer.isPlaying()) {
				playBackgroundSound();
				return true;
			} else {
				pauseBackgroundSound();
				return false;
			}
		}
		return false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Hilos.isSoundVibration()
				&& ((getBackgroundSoundBehavior() & BGSOUND_START) != 0)) {
			playBackgroundSound();
		}
	}

	protected void playBackgroundSound() {
		if (mMediaPlayer != null && !isBackgroundSoundPlaying()) {
			mMediaPlayer.start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Hilos.isSoundVibration()
				&& ((getBackgroundSoundBehavior() & BGSOUND_PAUSE) != 0)) {
			playBackgroundSound();
		}
	}

	@Override
	protected void onPause() {
		if (Hilos.isSoundVibration()
				&& ((getBackgroundSoundBehavior() & BGSOUND_PAUSE) != 0)) {
			pauseBackgroundSound();
		}
		super.onPause();
	}

	protected void pauseBackgroundSound() {
		if (mMediaPlayer != null && isBackgroundSoundPlaying()) {
			mMediaPlayer.pause();
		}
	}

	@Override
	protected void onStop() {
		if (Hilos.isSoundVibration()
				&& ((getBackgroundSoundBehavior() & BGSOUND_STOP) != 0)) {
			stopBackgroundSound();
		}
		super.onStop();
	}

	protected void stopBackgroundSound() {
		if (mMediaPlayer != null && isBackgroundSoundPlaying()) {
			mMediaPlayer.stop();
		}
	}

	@Override
	public void onDestroy() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		if (mSpeechEngine != null) {
			mSpeechEngine.destroy();
		}
		super.onDestroy();
	}

	public boolean hasSpeech() {
		return true;
	}

	private class ShowToastInParts extends Thread {
		private String toast;
		private boolean canceled;

		public ShowToastInParts(String toast) {
			this.toast = toast;
		}

		public void cancel() {
			canceled = true;
			if (last != null) {
				last.cancel();
			}
		}

		private Toast last;

		@Override
		public void run() {
			String[] resized = resize(toast);
			for (final String part : resized) {
				if (canceled) {
					break;
				}
				if (last != null) {
					last.cancel();
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						(last = Toast.makeText(BaseActivity.this, part,
								Toast.LENGTH_LONG)).show();
					}
				});
				// try {
				// Thread.sleep((int) (2000 * part.length() / RESIZETO));
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
			}
		}

		private static final int RESIZETO = 50;

		private String[] resize(String text) {
			List<String> parts = new ArrayList<String>();
			String[] split1 = text.split("\\.");
			for (String s : split1) {
				int n;
				boolean done = false;
				do {
					s = s.trim();
					if (s.length() <= RESIZETO) {
						n = s.length();
					} else {
						n = RESIZETO;
						while (n >= 0 && !Character.isSpaceChar(s.charAt(n))) {
							n--;
						}
						if (n < 0) {
							n = RESIZETO;
						}
					}
					String part = s.substring(0, n);
					if (n < s.length()
							&& (s = s.substring(n).trim()).length() > 0) {
						part += "...";
					} else {
						part += ".";
						done = true;
					}
					parts.add(part);
				} while (!done);
			}
			return parts.toArray(new String[0]);
		}
	}

	private ShowToastInParts mLastShowToast;
	private Toast mLastToast;

	@Override
	public void speechBegin(final String text) {
		// if (mLastShowToast != null) {
		// mLastShowToast.cancel();
		// }
		// (mLastShowToast = new ShowToastInParts(text)).start();
		if (mLastToast != null) {
			mLastToast = null;
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				(mLastToast = Toast.makeText(BaseActivity.this, text,
						Toast.LENGTH_LONG)).show();
			}
		});
	}

	@Override
	public void speechEnd() {
	}
}

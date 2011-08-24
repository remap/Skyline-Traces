package edu.ucla.tft.hilos.lightbox;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import edu.ucla.tft.hilos.BaseActivity;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.Keywords.Keyword;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.Resource;
import edu.ucla.tft.hilos.ResourceFilterByAttribute;
import edu.ucla.tft.hilos.StyleInfo;
import edu.ucla.tft.hilos.lightbox.LightBoxItem.Caption;
import edu.ucla.tft.hilos.lightbox.LightBoxItemProvider.LoadCallback;
import edu.ucla.tft.hilos.lightbox.LightBoxItemProvider.ResourceLightBoxItemProvider;

/**
 * An activity to show images and play audio and video. Provides a slide-show
 * like interface with a list of thumbnails for all media items at the bottom, a
 * list of keywords and a preview of the media item itself. Recognizes gestures
 * such as flip to next/prev item, tap to view full-screen etc.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class LightBoxActivity extends BaseActivity implements
		LightBoxItemContainer, OnBufferingUpdateListener, OnCompletionListener,
		OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback,
		KeywordList.Listener {
	public static LightBoxItemProvider UseProvider;
	public static final int NORMAL = 0, EXPANDED = 1;

	private int state = NORMAL;
	private LightBoxView mLightBoxView;

	private List<LightBoxItemContainer.Listener> mListeners;
	private LightBoxItem[] mLightBoxItems;
	private int mLightBoxSelectedItem = -1;
	private int mLastLightBoxSelectedItem = mLightBoxSelectedItem;
	private ViewGroup mNormalDisplay;
	private ViewGroup mExpandedDisplay;
	private LightBoxShow mLightBoxShowNormal;
	private LightBoxShow mLightBoxShowExpanded;
	private TextView mCaption;
	private KeywordList mKeywordList;
	private ThumbnailList mThumbnailList;

	private LightBoxItemProvider mProvider;
	private LoadCallback mLoadCallback;
	private boolean[] mLoadingLightBoxItem;

	private MediaPlayer mMediaPlayer;
	private SeekBar mPosition;
	private SeekBarUpdater updater;
	private static Handler sVideoHandler = new Handler();
	private int mVideoWidth;
	private int mVideoHeight;
	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;
	private SurfaceView mPreviewNormal;
	private SurfaceView mPreviewExpanded;
	private SurfaceHolder mHolderNormal;
	private SurfaceHolder mHolderExpanded;

	private Button mPrev;
	private Button mNext;
	private Button mMinimize;
	private Button mPlayStop;
	private Button mVolume;

	private Stack<LightBoxItemProvider> mProviderStack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLightBoxView = (LightBoxView) LayoutInflater.from(this).inflate(
				R.layout.lightbox_view, null);
		setContentView(mLightBoxView);

		mListeners = new LinkedList<LightBoxItemContainer.Listener>();

		mProviderStack = new Stack<LightBoxItemProvider>();
		mProvider = UseProvider != null ? UseProvider
				: new ResourceLightBoxItemProvider();
		int lightBoxItemsCount = mProvider.getLightBoxItemsCount();
		mLightBoxItems = new LightBoxItem[lightBoxItemsCount];
		mLoadCallback = new LoadCallback() {
			@Override
			public void error(int index, String message) {
			}

			@Override
			public void done(int index, LightBoxItem item) {
				mLightBoxItems[index] = item;
				setLightBoxItemSelected(mLightBoxSelectedItem);
			}
		};
		mNormalDisplay = (ViewGroup) findViewById(R.id.normalDisplay);
		mExpandedDisplay = (ViewGroup) findViewById(R.id.expandedDisplay);
		mLightBoxShowExpanded = (LightBoxShow) findViewById(R.id.lightBoxShowExpanded);
		mLightBoxShowExpanded.setLightBoxItemContainer(this);
		mLightBoxShowExpanded.setControlsEnabled(true);
		mLightBoxShowNormal = (LightBoxShow) findViewById(R.id.lightBoxShowNormal);
		mLightBoxShowNormal.setLightBoxItemContainer(this);
		mCaption = (TextView) findViewById(R.id.caption);
		mKeywordList = (KeywordList) findViewById(R.id.keywordlist);
		mKeywordList.setLightBoxItemContainer(this);
		mKeywordList.setListener(this);
		mKeywordList.setVisibility(lightBoxItemsCount > 0 ? View.VISIBLE
				: View.INVISIBLE);
		mThumbnailList = (ThumbnailList) findViewById(R.id.thumbnaillist);
		mThumbnailList.setLightBoxItemContainer(this);
		mThumbnailList.setVisibility(lightBoxItemsCount > 0 ? View.VISIBLE
				: View.INVISIBLE);
		mPrev = (Button) findViewById(R.id.prev);
		mPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setLightBoxItemSelected(mLightBoxSelectedItem - 1);
			}
		});
		mPrev.setVisibility(lightBoxItemsCount > 1 ? View.VISIBLE
				: View.INVISIBLE);
		mNext = (Button) findViewById(R.id.next);
		mNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setLightBoxItemSelected(mLightBoxSelectedItem + 1);
			}
		});
		mNext.setVisibility(lightBoxItemsCount > 1 ? View.VISIBLE
				: View.INVISIBLE);
		mPlayStop = (Button) findViewById(R.id.playPause);
		mPlayStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playStop();
			}
		});
		mPlayStop.setVisibility(View.INVISIBLE);
		mVolume = (Button) findViewById(R.id.volume);
		mVolume.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		mVolume.setVisibility(View.INVISIBLE);
		mPosition = (SeekBar) findViewById(R.id.position);
		mPosition.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				seekTo(seekBar.getProgress() * 1000);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		mPosition.setVisibility(View.INVISIBLE);
		mMinimize = (Button) findViewById(R.id.minimize);
		mMinimize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stop();
				finish();
			}
		});

		mPreviewNormal = (SurfaceView) findViewById(R.id.surfaceNormal);
		mHolderNormal = mPreviewNormal.getHolder();
		mHolderNormal.addCallback(this);
		mHolderNormal.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mPreviewExpanded = (SurfaceView) findViewById(R.id.surfaceExpanded);
		mHolderExpanded = mPreviewExpanded.getHolder();
		mHolderExpanded.addCallback(this);
		mHolderExpanded.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		setLightBoxItemSelected(0);
	}

	public int getState() {
		return state;
	}

	protected void setState(int state) {
		this.state = state;
		switch (state) {
		case NORMAL:
			mExpandedDisplay.setVisibility(View.INVISIBLE);
			mNormalDisplay.setVisibility(View.VISIBLE);
			break;
		case EXPANDED:
			mNormalDisplay.setVisibility(View.INVISIBLE);
			mExpandedDisplay.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (state == EXPANDED) {
			setState(NORMAL);
		} else {
			if (!mProviderStack.isEmpty()) {
				setProvider(mProviderStack.pop());
			} else {
				super.onBackPressed();
			}
		}
	}

	@Override
	public LightBoxItem getLightBoxItemAt(int index) {
		if (index < 0 || index >= mLightBoxItems.length) {
			return null;
		}
		if (mLoadingLightBoxItem == null
				|| mLoadingLightBoxItem.length != mLightBoxItems.length) {
			mLoadingLightBoxItem = new boolean[mLightBoxItems.length];
		}
		if (mLightBoxItems[index] == null && !mLoadingLightBoxItem[index]) {
			mLoadingLightBoxItem[index] = true;
			mProvider.loadLightBoxItemAt(this, index, mLoadCallback);
		}
		return mLightBoxItems[index];
	}

	@Override
	public int getLightBoxItemSelected() {
		return mLightBoxSelectedItem;
	}

	public void setLightBoxItemSelected(int index) {
		stop(); // Stop play back
		LightBoxItem item = null;
		int total = mLightBoxItems.length;
		if (total > 0) {
			mLightBoxSelectedItem = index % total;
			if (mLightBoxSelectedItem < 0) {
				mLightBoxSelectedItem += total;
			}
			item = mLightBoxItems[mLightBoxSelectedItem];
		} else {
			item = null;
			mLightBoxSelectedItem = -1;
		}
		final boolean isMediaType = isMediaType(item);
		final boolean requiresDrawable = requiresDrawable(item);
		final boolean requiresSurface = requiresSurface(item);
		Caption[] captions;
		final String captionText = item == null
				|| (captions = item.getCaptions()) == null
				|| captions.length == 0 ? "" : captions[0].text;
		mLightBoxView.post(new Runnable() {
			@Override
			public void run() {
				mPlayStop.setVisibility(isMediaType ? View.VISIBLE
						: View.INVISIBLE);
				// mVolume.setVisibility(isMediaType ? View.VISIBLE
				// : View.INVISIBLE);
				// mPosition.setVisibility(isMediaType ? View.VISIBLE
				// : View.INVISIBLE);
				mLightBoxShowNormal
						.setVisibility(requiresDrawable ? View.VISIBLE
								: View.INVISIBLE);
				mPreviewNormal.setVisibility(requiresSurface ? View.VISIBLE
						: View.INVISIBLE);
				mCaption.setText(captionText);
			}
		});
		notifySelectionChanged();
	}

	@Override
	public int getLightBoxItemsCount() {
		return mLightBoxItems.length;
	}

	@Override
	public void onLaunchGesture() {
		switch (state) {
		case NORMAL:
			setState(EXPANDED);
			break;
		case EXPANDED:
			setState(NORMAL);
			break;
		}
	}

	@Override
	public LightBoxItemProvider getProvider() {
		return mProvider;
	}

	@Override
	public void setProvider(LightBoxItemProvider provider) {
		releaseLightBoxItems(); // clean up any items held by the previous
								// provider
		mProvider = provider;
		final int n = mProvider.getLightBoxItemsCount();
		mLightBoxView.post(new Runnable() {
			@Override
			public void run() {
				mPrev.setVisibility(n > 1 ? View.VISIBLE : View.INVISIBLE);
				mNext.setVisibility(n > 1 ? View.VISIBLE : View.INVISIBLE);
			}
		});
		mLoadingLightBoxItem = null;
		mLightBoxItems = new LightBoxItem[n];
		setLightBoxItemSelected(0);
	}

	@Override
	public void addListener(Listener l) {
		if (mListeners.contains(l)) {
			return;
		}
		mListeners.add(l);
	}

	@Override
	public void removeListener(Listener l) {
		mListeners.remove(l);
	}

	protected void notifySelectionChanged() {
		LightBoxItem item = getLightBoxItemAt(getLightBoxItemSelected());
		if (item != null && mLastLightBoxSelectedItem != mLightBoxSelectedItem) {
			mLastLightBoxSelectedItem = mLightBoxSelectedItem;
			Caption[] captions;
			if ((captions = item.getCaptions()) != null) {
				int c = 0;
				for (Caption caption : captions) {
					if (c == 0) {
						mSpeechEngine.speakImmediately(caption.text);
					} else {
						mSpeechEngine.speakQueued(caption.text);
					}
				}
			}
		}
		for (LightBoxItemContainer.Listener l : mListeners) {
			l.selectionChanged();
		}
	}

	@Override
	public void onKeywordSelected(final String keywordText) {
		mProviderStack.push(mProvider);
		ResourceLightBoxItemProvider provider = new ResourceLightBoxItemProvider();
		provider.setFilter(new ResourceFilterByAttribute("-landmark_overlay",
				"-landmark_thumbnail", "-style") {
			@Override
			public boolean accept(Resource resource) {
				if (resource.keywords != null) {
					for (Keyword keyword : resource.keywords.toArray()) {
						if (keywordText.equals(keyword.text)) {
							return super.accept(resource);
						}
					}
				}
				return false;
			}
		});
		setProvider(provider);
	}

	private boolean isMediaType(LightBoxItem item) {
		return item != null && (item.isAudioType() || item.isVideoType());
	}

	private boolean requiresDrawable(LightBoxItem item) {
		return item != null && (item.isImageType() || item.isAudioType());
	}

	private boolean requiresSurface(LightBoxItem item) {
		return item != null && item.isVideoType();
	}

	private void stop() {
		if (mMediaPlayer == null) {
			return;
		}
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer = null;
		mPosition.setProgress(0);
		mPlayStop.setBackgroundDrawable(Hilos.getCurrStyleInfo()
				.getStateListDrawable(StyleInfo.StyleStateListId.btn_play));
	}

	private void playStop() {
		LightBoxItem item = mLightBoxItems[mLightBoxSelectedItem];
		if (item.isAudioType()) {
			playStopAudio(item);
		} else if (item.isVideoType()) {
			playStopVideo(item);
		}
	}

	private void playStopAudio(LightBoxItem item) {
		if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
			mMediaPlayer = MediaPlayer.create(this,
					Uri.parse((String) item.getData()));
			if (mMediaPlayer == null) {
				return;
			}
			mPosition.setMax(mMediaPlayer.getDuration() / 1000);
			mMediaPlayer.start();
			mPosition.postDelayed(updater = new SeekBarUpdater(mMediaPlayer),
					1000);
			mPlayStop
					.setBackgroundDrawable(Hilos.getCurrStyleInfo()
							.getStateListDrawable(
									StyleInfo.StyleStateListId.btn_pause));
		} else {
			stop();
			if (updater != null) {
				updater.cancel();
			}
		}
	}

	private void seekTo(int position_msec) {
		if (mMediaPlayer == null) {
			return;
		}
		mPosition.setProgress(position_msec / 1000);
		mMediaPlayer.seekTo(position_msec);
	}

	private void playStopVideo(LightBoxItem item) {
		if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
			doCleanUp();
			try {
				// Create a new media player and set the listeners
				mMediaPlayer = new MediaPlayer();
				mMediaPlayer.setDataSource((String) item.getData());
				mMediaPlayer.setDisplay(mHolderNormal);
				mMediaPlayer.prepare();
				mMediaPlayer.setOnBufferingUpdateListener(this);
				mMediaPlayer.setOnCompletionListener(this);
				mMediaPlayer.setOnPreparedListener(this);
				mMediaPlayer.setOnVideoSizeChangedListener(this);
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mPlayStop.setBackgroundDrawable(Hilos.getCurrStyleInfo()
						.getStateListDrawable(
								StyleInfo.StyleStateListId.btn_pause));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			stop();
			if (updater != null) {
				updater.cancel();
			}
		}
	}

	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
	}

	public void onCompletion(MediaPlayer arg0) {
		stop();
	}

	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		if (width == 0 || height == 0) {
			return;
		}
		mIsVideoSizeKnown = true;
		mVideoWidth = width;
		mVideoHeight = height;
		int normWidth = mNormalDisplay.getWidth(), normHeight = mNormalDisplay
				.getHeight();
		double normScale = Math.min((double) normWidth / width,
				(double) normHeight / height);
		LayoutParams normSizeParams = new LayoutParams(
				(int) (width * normScale), (int) (height * normScale));
		normSizeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mPreviewNormal.setLayoutParams(normSizeParams);
		int expWidth = mExpandedDisplay.getWidth(), expHeight = mExpandedDisplay
				.getHeight();
		double expScale = Math.min((double) expWidth / width,
				(double) expHeight / height);
		LayoutParams expSizeParams = new LayoutParams((int) (width * expScale),
				(int) (height * expScale));
		expSizeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mPreviewExpanded.setLayoutParams(expSizeParams);
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	public void onPrepared(MediaPlayer mediaplayer) {
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	protected void onStop() {
		super.onStop();
		releaseMediaPlayer();
		doCleanUp();
		releaseLightBoxItems();
	}

	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void doCleanUp() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}

	@Override
	public void releaseLightBoxItems() {
		if (mLightBoxItems != null) {
			for (LightBoxItem item : mLightBoxItems) {
				if (item != null) {
					item.releaseData();
				}
			}
		}
	}

	private void startVideoPlayback() {
		mMediaPlayer.start();
	}

	private class SeekBarUpdater implements Runnable {
		private MediaPlayer player;
		private boolean canceled;

		public SeekBarUpdater(MediaPlayer player) {
			this.player = player;
		}

		public void cancel() {
			this.canceled = true;
		}

		@Override
		public void run() {
			if (!canceled && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				mPosition.setProgress(player.getCurrentPosition() / 1000);
				mPosition.postDelayed(this, 1000);
			}
		}
	}
}
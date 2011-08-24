package edu.ucla.tft.hilos.lightbox;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.StyleInfo;
import edu.ucla.tft.hilos.StyleInfo.StyleStateListId;
import edu.ucla.tft.hilos.lightbox.LightBoxItemProvider.LoadCallback;

/**
 * A transparent overlay to display a 'mini' view of the current set of media
 * items in the map or skyline view, in the top-right corner. keyword.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class LightBoxMini extends FrameLayout implements LightBoxItemContainer {
	private List<LightBoxItemContainer.Listener> mListeners;
	private LightBoxItem[] mLightBoxItems;
	private int mLightBoxSelectedItem;
	private LightBoxShow mLightBoxShow;
	private TextView mTitle;

	private LightBoxItemProvider mProvider;
	private LoadCallback mLoadCallback;
	private boolean[] mLoadingLightBoxItem;
	private Button mPrev;
	private Button mNext;

	public LightBoxMini(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LightBoxMini(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LightBoxMini(Context context) {
		super(context);
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		mListeners = new LinkedList<Listener>();
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
		mLightBoxShow = (LightBoxShow) findViewById(R.id.lightBoxShow);
		mLightBoxShow.setLightBoxItemContainer(this);
		mTitle = (TextView) findViewById(R.id.mini_title);
		StyleInfo currStyleInfo = Hilos.getCurrStyleInfo();
		mPrev = (Button) findViewById(R.id.mini_prev);
		mPrev.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleStateListId.btn_prev));
		mPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setLightBoxItemSelected(mLightBoxSelectedItem - 1);
			}
		});
		mNext = (Button) findViewById(R.id.mini_next);
		mNext.setBackgroundDrawable(currStyleInfo
				.getStateListDrawable(StyleStateListId.btn_next));
		mNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setLightBoxItemSelected(mLightBoxSelectedItem + 1);
			}
		});
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
		post(new Runnable() {
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
	public LightBoxItem getLightBoxItemAt(int index) {
		if (mProvider == null || index < 0 || index >= mLightBoxItems.length) {
			return null;
		}
		if (mLoadingLightBoxItem == null
				|| mLoadingLightBoxItem.length != mLightBoxItems.length) {
			mLoadingLightBoxItem = new boolean[mLightBoxItems.length];
		}
		if (mLightBoxItems[index] == null && !mLoadingLightBoxItem[index]) {
			mLoadingLightBoxItem[index] = true;
			mProvider.loadLightBoxItemAt(getContext(), index, mLoadCallback);
		}
		return mLightBoxItems[index];
	}

	@Override
	public int getLightBoxItemSelected() {
		return mLightBoxSelectedItem;
	}

	public void setLightBoxItemSelected(int index) {
		LightBoxItem item = null;
		int total = mLightBoxItems.length;
		if (total > 0) {
			while (index < 0) {
				index += total;
			}
			mLightBoxSelectedItem = index % total;
			item = getLightBoxItemAt(index);
		} else {
			item = null;
			mLightBoxSelectedItem = -1;
		}
		final String titleText = item == null ? "" : item.getTitle();
		post(new Runnable() {
			@Override
			public void run() {
				mTitle.setText(titleText);
				invalidate();
			}
		});
		notifySelectionChanged();
	}

	@Override
	public int getLightBoxItemsCount() {
		return mLightBoxItems.length;
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

	@Override
	public void onLaunchGesture() {
		Context ctx = getContext();
		LightBoxActivity.UseProvider = mProvider;
		Intent intent = new Intent(ctx, LightBoxActivity.class);
		ctx.startActivity(intent);
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
		for (LightBoxItemContainer.Listener l : mListeners) {
			l.selectionChanged();
		}
	}
}
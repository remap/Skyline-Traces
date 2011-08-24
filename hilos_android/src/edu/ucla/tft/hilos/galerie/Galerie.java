package edu.ucla.tft.hilos.galerie;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import edu.ucla.tft.hilos.AnimationThread;
import edu.ucla.tft.hilos.Hilos;
import edu.ucla.tft.hilos.Network;
import edu.ucla.tft.hilos.R;
import edu.ucla.tft.hilos.Resource;
import edu.ucla.tft.hilos.StyleInfo;

public class Galerie extends Activity {
	private static final int MI_LAYOUT = 0;
	private static final Layout[] sLayouts;
	static {
		List<Layout> list = new LinkedList<Layout>();
		list.add(new Layout.Linear());
		list.add(new Layout.Radial());
		list.add(new Layout.Geo());
		sLayouts = list.toArray(new Layout[0]);
	}
	private GalerieView view;
	private GalerieRenderer renderer;
	private AnimationThread animThread;
	private Layout layout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		StyleInfo styleInfo = Hilos.getCurrStyleInfo();
		setTheme(styleInfo.systemTheme);

		view = new GalerieView(this);
		renderer = new GalerieRenderer(this);
		view.setRenderer(renderer);
		layout = new Layout.Linear();
		setContentView(view);
		loadResources();
	}

	private void doLayout() {
		for (FloatingImage fi : renderer.getImages()) {
			layout.layout((ResourceFloatingImage) fi);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MI_LAYOUT, Menu.NONE, "Layout").setIcon(
				R.drawable.ic_menu_layout);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MI_LAYOUT:
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("Layout");
			String[] str = new String[sLayouts.length];
			for (int i = 0; i < sLayouts.length; i++) {
				str[i] = sLayouts[i].name();
			}
			DialogInterface.OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					layout = sLayouts[which];
					doLayout();
				}
			};
			b.setItems(str, listener);
			b.create().show();
			break;
		}
		return true;
	}

	private List<ResourceFloatingImage> mResourceList = new ArrayList<ResourceFloatingImage>();

	private void loadResources() {
		new Thread() {
			@Override
			public void run() {
				Resource[] resources = Network.getResourceList(null, false);
				mResourceList.clear();
				int count = 0;
				for (Resource res : resources) {
					if (!Resource.isImageType(res.resourceType)) {
						continue;
					}
					mResourceList.add(new ResourceFloatingImage(count++, res));
					if (count >= 50) {
						break;
					}
				}
				renderer.setImages(mResourceList
						.toArray(new ResourceFloatingImage[0]));
				doLayout();
				view.startAnimation();
			}
		}.start();
	}
}
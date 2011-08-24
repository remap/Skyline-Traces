package edu.ucla.tft.hilos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * An activity to show all cached items. Provides options to delete individual
 * or all element(s).
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class CacheList extends ListActivity {
	protected ListView mListView;
	protected File[] mFiles;
	protected int mSelection = -1;

	protected String getDirName() {
		return "cache";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		updateFilesList();

		mListView = getListView();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mSelection = position;
			}
		});
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	private void updateFilesList() {
		List<String> names = new ArrayList<String>();
		List<File> files = new ArrayList<File>();
		File dir = new File(Environment.getExternalStorageDirectory()
				+ "/hilos/" + getDirName() + "/");
		for (File f : dir.listFiles()) {
			names.add(f.getName());
			files.add(f);
		}
		mFiles = files.toArray(new File[0]);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				names.toArray(new String[names.size()])));
	}

	private static final int DELETE = 0, DELETE_ALL = 1;

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, DELETE, Menu.NONE, "Delete").setIcon(
				R.drawable.ic_menu_delete);
		menu.add(Menu.NONE, DELETE_ALL, Menu.NONE, "Delete All").setIcon(
				R.drawable.ic_menu_delete_all);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			return false;
		case DELETE:
			if (mSelection < 0 || mSelection >= mFiles.length) {
				return false;
			}
			deleteSelectedFile(mFiles[mSelection]);
			break;
		case DELETE_ALL:
			deleteAllFiles();
			break;
		}
		return true;
	}

	protected void deleteSelectedFile(File file) {
		file.delete();
		mSelection = -1;
		updateFilesList();
	}

	protected void deleteAllFiles() {
		for (int i = 0; i < mFiles.length; i++) {
			mFiles[i].delete();
		}
		mSelection = -1;
		updateFilesList();
	}
}

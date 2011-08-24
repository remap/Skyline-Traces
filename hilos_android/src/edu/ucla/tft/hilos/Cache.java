package edu.ucla.tft.hilos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

/**
 * A singleton class used for caching results on secondary storage (sdcard) to
 * be retrieved in the future. A user inputs a key string along with an input
 * stream from which content to be stored is read. The content is read and
 * stored in a file.Subsequently the same key can be used to retrieve an input
 * stream pointing to the stored file.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Cache {
	private static File sAppDir = new File(
			Environment.getExternalStorageDirectory() + "/hilos/cache");
	static {
		if (!sAppDir.exists()) {
			sAppDir.mkdirs();
		}
	}

	private Cache() {
	}

	private static File forKey(String key) {
		return new File(sAppDir + "/" + Utils.md5(key));
	}

	public static boolean exists(String key) {
		return forKey(key).exists();
	}

	public static InputStream load(String key) {
		File file = forKey(key);
		if (!file.exists()) {
			return null;
		}
		InputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return in;
	}

	public static void store(String key, InputStream in) {
		store(key, in, true);
	}

	public static boolean store(String key, InputStream in, boolean overwrite) {
		File file = forKey(key);
		if (file.exists() && !overwrite) {
			return false;
		}
		try {
			OutputStream out = new FileOutputStream(file);
			Utils.readFully(in, out);
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static InputStream copy(String key, final InputStream in) {
		store(key, in);
		return load(key);
	}
}

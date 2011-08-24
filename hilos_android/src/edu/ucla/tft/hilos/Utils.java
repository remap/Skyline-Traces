package edu.ucla.tft.hilos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import min3d.core.Object3dContainer;
import min3d.parser.IParser;
import min3d.parser.Parser;
import min3d.parser.ResourceProvider;
import min3d.vos.Number3d;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import edu.ucla.tft.hilos.Keywords.Keyword;
import edu.ucla.tft.hilos.Scene.Model;

/**
 * Some common utility methods.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Utils {
	public static void readFully(Reader r, Writer w) throws IOException {
		char[] buf = new char[4096];
		int n;
		while ((n = r.read(buf)) > 0) {
			w.write(buf, 0, n);
		}
		w.flush();
	}

	public static void readFully(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[4096];
		int n;
		while ((n = in.read(buf)) > 0) {
			out.write(buf, 0, n);
		}
		out.flush();
	}

	public static String timestamp() {
		Date d = new Date();
		return (d.getMonth() + 1) + "/" + d.getDate() + "/"
				+ (d.getYear() + 1900) + "/" + d.getHours() + ":"
				+ d.getMinutes() + "::" + d.getSeconds();
	}

	public static String join(String sep, int[] args) {
		Integer[] array = new Integer[args.length];
		for (int i = 0; i < args.length; i++) {
			array[i] = args[i];
		}
		return join(sep, (Object[]) array);
	}

	public static String join(String sep, Object... args) {
		StringBuilder sb = new StringBuilder();
		int c = 0;
		for (Object o : args) {
			sb.append((c++ > 0 ? sep : "") + o);
		}
		return sb.toString();
	}

	public static String dateToString(Date date) {
		return (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-"
				+ date.getDate();
	}

	public static Date stringToDate(String string) {
		Date date = new Date();
		if (string != null) {
			String[] tok = string.split("-");
			if (tok.length == 3) {
				try {
					int y = Integer.parseInt(tok[0]);
					int m = Integer.parseInt(tok[1]);
					int d = Integer.parseInt(tok[2]);
					date.setDate(d);
					date.setMonth(m - 1);
					date.setYear(y - 1900);
				} catch (NumberFormatException e) {
				}
			}
		}
		return date;
	}

	public static String toCamelCase(String s) {
		char[] array = s.toCharArray();
		StringBuilder sb = new StringBuilder();
		boolean capitalize = true;
		for (char c : array) {
			if (Character.isSpace(c)) {
				capitalize = true;
				continue;
			}
			if (!Character.isLetter(c)) {
				continue;
			}
			if (capitalize) {
				c = Character.toUpperCase(c);
				capitalize = false;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static String md5(String s) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuilder hash = new StringBuilder();
			for (int i = 0; i < messageDigest.length; i++)
				hash.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hash.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Bitmap createThumb(Bitmap bitmap, int maxSize, int borderSize) {
		int imgWidth = bitmap.getWidth();
		int imgHeight = bitmap.getHeight();
		int imgThumbSize = (maxSize - 2 * borderSize);
		double scale = Math.min((double) imgThumbSize / imgWidth,
				(double) imgThumbSize / imgHeight);
		int scaledWidth = (int) (imgWidth * scale);
		int scaledHeight = (int) (imgHeight * scale);
		int thumbWidth = scaledWidth + 2 * borderSize;
		int thumbHeight = scaledHeight + 2 * borderSize;
		Bitmap buf = Bitmap.createBitmap(thumbWidth, thumbHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(buf);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.FILL);
		canvas.drawRect(0, 0, scaledWidth + 2 * borderSize, scaledHeight + 2
				* borderSize, paint);
		paint.setColor(Color.GRAY);
		paint.setStyle(Style.STROKE);
		canvas.drawRect(0, 0, scaledWidth + 2 * borderSize - 1, scaledHeight
				+ 2 * borderSize - 1, paint);
		canvas.drawBitmap(bitmap,
				new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
				new RectF(borderSize, borderSize, borderSize + scaledWidth,
						borderSize + scaledHeight), null);
		return buf;
	}

	public static Resource[] filterResources(Resource[] resources,
			ResourceFilter filter) {
		List<Resource> list = new LinkedList<Resource>();
		for (Resource resource : resources) {
			if (filter.accept(resource)) {
				list.add(resource);
			}
		}
		return list.toArray(new Resource[0]);
	}

	public static Resource findResourceByAttribute(Resource[] resources,
			ResourceFilter filter) {
		for (Resource resource : resources) {
			if (filter.accept(resource)) {
				return resource;
			}
		}
		return null;
	}

	public static interface OnDeletionProgressListener {
		public void deleted(int deletedFileCount, int totalFileCount);
	}

	public static void clearMapCache(OnDeletionProgressListener l) {
		File sMapCacheDir = new File(Environment.getExternalStorageDirectory()
				+ "/osmdroid/tiles");
		List<File> files = new ArrayList<File>();
		List<File> directories = new ArrayList<File>();
		findFiles(sMapCacheDir, files, directories);
		int totalFileCount = files.size() + directories.size();
		int deletedFileCount = 0;
		for (File f : files) {
			f.delete();
			deletedFileCount++;
			if (l != null) {
				l.deleted(deletedFileCount, totalFileCount);
			}
		}
		for (File f : files) {
			f.delete();
			deletedFileCount++;
			if (l != null) {
				l.deleted(deletedFileCount, totalFileCount);
			}
		}
		for (File d : directories) {
			d.delete();
			deletedFileCount++;
			if (l != null) {
				l.deleted(deletedFileCount, totalFileCount);
			}
		}
	}

	public static void findFiles(File root, List<File> files,
			List<File> directories) {
		File[] fs = root.listFiles();
		if (fs == null) {
			return;
		}
		for (File f : fs) {
			if (f.isDirectory()) {
				findFiles(f, files, directories);
			} else {
				files.add(f);
			}
		}
		directories.add(root);
	}

	public static void delete(File dir) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				delete(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}

	public static URL constructURL(String url_str, Map<String, String> params)
			throws MalformedURLException {
		if (params != null) {
			int c = 0;
			for (Entry<String, String> e : params.entrySet()) {
				url_str += (c++ == 0 ? "?" : "&") + e.getKey() + "="
						+ URLEncoder.encode(e.getValue());
			}
		}
		return new URL(url_str);
	}

	public static URL constructURL(String url_str, Object... params)
			throws MalformedURLException {
		for (int i = 0; i < params.length; i += 2) {
			url_str += (i == 0 ? "?" : "&") + params[i] + "="
					+ URLEncoder.encode(params[i + 1].toString());
		}
		return new URL(url_str);
	}

	public static String[] getTopKeywordNames(Keywords[] keywordsArray,
			int maxKeywordsCount) {
		List<Keyword> set = new ArrayList<Keyword>();
		for (Keywords keywords : keywordsArray) {
			for (Keyword keyword : keywords.toArray()) {
				int n;
				if ((n = set.indexOf(keyword)) >= 0) {
					Keyword old = set.get(n);
					old.weight += keyword.weight;
				} else {
					set.add(keyword);
				}
			}
		}
		float totalSum = 0;
		for (Keyword keyword : set) {
			totalSum += keyword.weight;
		}
		List<String> keywords = new ArrayList<String>();
		maxKeywordsCount = maxKeywordsCount <= 0 ? set.size() : Math.min(
				maxKeywordsCount, set.size());
		if (maxKeywordsCount > 0) {
			while (keywords.size() < maxKeywordsCount) {
				float random = (float) (Math.random() * totalSum);
				float cumulative = 0;
				int i = 0;
				while (i < set.size()
						&& (cumulative += set.get(i).weight) < random) {
					i++;
				}
				if (i >= set.size()) {
					i--;
				}
				Keyword chosen = set.remove(i);
				totalSum -= chosen.weight;
				keywords.add(chosen.text);
			}
		}
		return keywords.toArray(new String[0]);
	}

	private static class NetworkResourceProvider implements ResourceProvider {
		@Override
		public boolean exists(String resourceID) {
			return true;
		}

		public InputStream readResource(String resourceID) {
			String subPath = resourceID.substring(resourceID.indexOf(":") + 1);
			return Network.getModelInputStream(subPath, false);
		}
	}

	private static ResourceProvider sProvider = new NetworkResourceProvider();

	public static Object3dContainer loadModel(Resources res, Model model) {
		IParser parser = Parser.createParser(Parser.Type.OBJ, sProvider,
				"edu.ucla.tft.hilos:raw/" + model.src, true);
		parser.parse();
		Object3dContainer obj = parser.getParsedObject();
		obj.position()
				.add(new Number3d(-model.anchor.x, -model.anchor.y,
						-model.anchor.z));
		obj.scale().setAllFrom(
				new Number3d(model.scale.x, model.scale.y, model.scale.z));
		obj.position().add(
				new Number3d(model.anchor.x, model.anchor.y, model.anchor.z));
		obj.position()
				.add(new Number3d(
						(model.location.x - Model.DEFAULT_LOCATION.x) * 5000,
						0, (model.location.y - Model.DEFAULT_LOCATION.y) * 5000));
		return obj;
	}
}

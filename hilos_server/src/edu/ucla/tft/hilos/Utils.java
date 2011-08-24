package edu.ucla.tft.hilos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Some common utility methods.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Utils {
	public static void empty(File file) {
		if (!file.isDirectory()) {
			return;
		}
		for (File f : file.listFiles()) {
			deleteAll(f);
		}
	}

	public static void deleteAll(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteAll(f);
			}
		}
		file.delete();
	}

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

	private static final String[] sMonths = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };

	public static int monthStrToInt(String s) {
		for (int i = 0; i < sMonths.length; i++) {
			if (sMonths[i].equalsIgnoreCase(s)) {
				return i + 1;
			}
		}
		return 0;
	}

	public static String monthIntToStr(int n) {
		return n > 0 && n <= sMonths.length ? sMonths[n - 1] : "";
	}

	public static String randomAlphaNum(int length) {
		byte[] buf = new byte[length];
		for (int i = 0; i < buf.length; i++) {
			int n = (int) (Math.random() * 62);
			if (n < 10) {
				buf[i] = (byte) (0x30 + n);
			} else if (n < 36) {
				n -= 10;
				buf[i] = (byte) (0x41 + n);
			} else {
				n -= 36;
				buf[i] = (byte) (0x61 + n);
			}
		}
		return new String(buf);
	}

	private static final List<String> sLanguagees = new LinkedList<String>();
	static {
		sLanguagees.add("en");
		sLanguagees.add("es");
		sLanguagees.add("zh");
	}

	public static final String TR(Connection conn, String var, String lang) {
		if (lang == null || !sLanguagees.contains(lang)) {
			lang = sLanguagees.get(0);
		}
		if (!var.startsWith("$")) {
			return var;
		}
		var = var.substring(1);
		try {
			Statement stmt = conn.createStatement();
			ResultSet set = stmt.executeQuery(String.format(
					"select %s from strings where name='%s'", lang, var));
			if (set.next()) {
				return set.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
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

	private static final Map<String, String> sDefaultGallery2FormatMap = new HashMap<String, String>();
	static {
		sDefaultGallery2FormatMap.put("GalleryPhotoItem", "jpg");
		sDefaultGallery2FormatMap.put("GalleryMovieItem", "mp4");
		sDefaultGallery2FormatMap.put("GalleryAudioItem", "mp3");
		sDefaultGallery2FormatMap.put("GalleryUnknownItem", "mp3"); // sic
	}

	public static final String G2(String str) {
		String mapped_val = sDefaultGallery2FormatMap.get(str);
		if (mapped_val == null) {
			return null;
		}
		return mapped_val;
	}

	public static final void close(Object... objs) {
		for (Object obj : objs) {
			if (obj instanceof ResultSet) {
				try {
					((ResultSet) obj).close();
				} catch (SQLException e) {
					// ignore
				}
			} else if (obj instanceof Statement) {
				try {
					((Statement) obj).close();
				} catch (SQLException e) {
					// ignore
				}
			}
			if (obj instanceof Connection) {
				try {
					((Connection) obj).close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
	}
}

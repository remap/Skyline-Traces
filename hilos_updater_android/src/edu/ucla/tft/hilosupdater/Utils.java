package edu.ucla.tft.hilosupdater;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Class containing common utility functions.
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
}

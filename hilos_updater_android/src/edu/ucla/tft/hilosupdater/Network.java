package edu.ucla.tft.hilosupdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

/**
 * Utility class to perform actions which send/receive data over the network.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Network {
	private Network() {
	}

	public static class Version {
		public final int latestVersion;
		public final long fileSize;

		public Version(int latestVersion, long fileSize) {
			this.latestVersion = latestVersion;
			this.fileSize = fileSize;
		}
	}

	private static boolean opening;
	private static InputStream in;

	private static void openStream(final URL url) throws Exception {
		in = null;
		int waitFor = 5;
		opening = true;
		new Thread() {
			public void run() {
				try {
					in = url.openStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
				opening = false;
			}
		}.start();
		while (opening && --waitFor > 0) {
			Thread.sleep(1000);
		}
		if (in == null) {
			throw new Exception();
		}
	}

	public static Version getLatestVersion() {
		try {
			URL url = new URL(Updater.getHostname() + "/getUpdate?a=0");
			openStream(url);
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringWriter w = new StringWriter();
			Utils.readFully(r, w);
			r.close();
			w.close();
			JSONObject ret = new JSONObject(w.toString());
			return new Version(ret.getInt("latestVersion"),
					ret.getLong("fileSize"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream getUpdateInputStream() {
		try {
			URL url = new URL(Updater.getHostname() + "/getUpdate?a=1");
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("Resource not found!");
			}
			return httpConn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream getUpdateInputStream(int id, String resourceType) {
		try {
			URL url = new URL(Updater.getHostname() + "/getUpdate?a=1");
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("Resource not found!");
			}
			return httpConn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

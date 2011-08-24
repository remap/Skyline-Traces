package edu.ucla.tft.hilos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import edu.ucla.tft.hilos.Scene.Model;

/**
 * Utility class containing functions to perform operations requiring
 * communication with the server.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Network {
	private Network() {
	}

	private static Handler sNetworkRequestHandler;
	private static Handler sNetworkRequestHandlerAsync;

	public static void init() {
		sNetworkRequestHandler = new Handler();
		new Thread() {
			public void run() {
				Looper.prepare();
				sNetworkRequestHandlerAsync = new Handler();
				Looper.loop();
			}
		}.start();
	}

	public static void execute(Task task) {
		sNetworkRequestHandler.post(task);
	}

	public static void executeAsync(Task task) {
		sNetworkRequestHandlerAsync.post(task);
	}

	public static interface Task extends Runnable {
		public void error(String e);
	}

	private static abstract class Common implements Task {
		private InputStream in;
		private OutputStream out;
		private boolean opening;

		public static interface PostRequest {
			public String getURL();

			public Map<String, String> getParameters();

			public void writePostOutput(OutputStream out);

			public void onPostCompleted(InputStream in);
		}

		public static interface GetRequest {
			public String getURL();

			public Map<String, String> getParameters();

			public void readGetInput(InputStream in);
		}

		private static void readFully(InputStream in, OutputStream out)
				throws IOException {
			byte[] buf = new byte[4096];
			int n;
			while ((n = in.read(buf)) > 0) {
				out.write(buf, 0, n);
			}
			out.flush();
		}

		protected void doGet(GetRequest getReq) throws Exception {
			String url_str = getReq.getURL();
			final URL url = Utils.constructURL(url_str, getReq.getParameters());
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
			getReq.readGetInput(in);
			in.close();
		}

		protected void doPost(PostRequest postReq) throws Exception {
			final HttpURLConnection conn = (HttpURLConnection) new URL(
					postReq.getURL()).openConnection();
			out = null;
			int waitFor = 5;
			opening = true;
			new Thread() {
				public void run() {
					try {
						conn.setDoOutput(true);
						conn.setDoInput(true);
						conn.setDefaultUseCaches(false);
						conn.setRequestMethod("POST");
						out = conn.getOutputStream();
					} catch (IOException e) {
						e.printStackTrace();
					}
					opening = false;
				}
			}.start();
			while (opening && --waitFor > 0) {
				Thread.sleep(1000);
			}
			if (out == null) {
				throw new Exception();
			}
			postReq.writePostOutput(out);
			out.close();
			in = conn.getInputStream();
			postReq.onPostCompleted(in);
			in.close();
		}
	}

	public static abstract class LoginTask extends Common {
		private String error;

		private String email;
		private String password;
		private String lang;

		private int uid;

		public LoginTask(String email, String password, String lang) {
			this.email = email;
			this.password = password;
			this.lang = lang;
		}

		public abstract void done(int uid);

		public void run() {
			try {
				error = null;
				doGet(new GetRequest() {
					@Override
					public String getURL() {
						return Hilos.getHostname() + "/login";
					}

					public Map<String, String> getParameters() {
						Map<String, String> params = new HashMap<String, String>();
						params.put("email", email);
						params.put("password", password);
						params.put("lang", lang);
						return params;
					}

					@Override
					public void readGetInput(InputStream in) {
						try {
							BufferedReader r = new BufferedReader(
									new InputStreamReader(in));
							StringWriter w = new StringWriter();
							Utils.readFully(r, w);
							r.close();
							w.close();
							JSONObject json = new JSONObject(w.toString());
							String e;
							if ((e = json.optString("error")).length() != 0) {
								error = e;
								return;
							} else {
								uid = json.getInt("uid");
							}
						} catch (Exception e) {
							error = "Invalid response from service.";
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				error = "Unable to complete request.";
			}
			if (error == null) {
				done(uid);
			} else {
				error(error);
			}
		}
	}

	public static abstract class JoinTask extends Common {
		private String error;

		private String email;
		private String password;
		private String name;
		private String lang;

		public JoinTask(String email, String password, String name, String lang) {
			this.email = email;
			this.password = password;
			this.name = name;
			this.lang = lang;
		}

		public abstract void done();

		public void run() {
			try {
				error = null;
				doGet(new GetRequest() {
					@Override
					public String getURL() {
						return Hilos.getHostname() + "/join";
					}

					@Override
					public Map<String, String> getParameters() {
						Map<String, String> params = new HashMap<String, String>();
						params.put("email", email);
						params.put("password", password);
						params.put("name", name);
						params.put("lang", lang);
						return params;
					}

					@Override
					public void readGetInput(InputStream in) {
						try {
							BufferedReader r = new BufferedReader(
									new InputStreamReader(in));
							StringWriter w = new StringWriter();
							Utils.readFully(r, w);
							r.close();
							w.close();
							JSONObject json = new JSONObject(w.toString());
							String e;
							if ((e = json.optString("error")).length() != 0) {
								error = e;
								return;
							}
						} catch (Exception e) {
							error = "Invalid response from service.";
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				error = "Unable to complete request.";
			}
			if (error == null) {
				done();
			} else {
				error(error);
			}
		}
	}

	public static abstract class ResetPasswordTask extends Common {
		private String error;

		private String email;
		private String lang;

		public ResetPasswordTask(String email, String lang) {
			this.email = email;
			this.lang = lang;
		}

		public abstract void done();

		public void run() {
			try {
				error = null;
				doGet(new GetRequest() {
					@Override
					public String getURL() {
						return Hilos.getHostname() + "/resetPassword";
					}

					@Override
					public Map<String, String> getParameters() {
						Map<String, String> params = new HashMap<String, String>();
						params.put("email", email);
						params.put("lang", lang);
						return params;
					}

					@Override
					public void readGetInput(InputStream in) {
						try {
							BufferedReader r = new BufferedReader(
									new InputStreamReader(in));
							StringWriter w = new StringWriter();
							Utils.readFully(r, w);
							r.close();
							w.close();
							JSONObject json = new JSONObject(w.toString());
							String e;
							if ((e = json.optString("error")).length() != 0) {
								error = e;
								return;
							}
						} catch (Exception e) {
							error = "Invalid response from service.";
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				error = "Unable to complete request.";
			}
			if (error == null) {
				done();
			} else {
				error(error);
			}
		}
	}

	public static Resource[] getResourceList(ResourceFilter filter,
			boolean forceRefresh) {
		if (filter == null) {
			filter = ResourceFilter.NULL;
		}
		Resource[] resources;
		try {
			URL url = Utils.constructURL(Hilos.getHostname()
					+ "/getResourceList", "lang", Locale.getDefault()
					.getLanguage());
			InputStream in;
			String key = url.toString();
			if (Cache.exists(key) && !forceRefresh) {
				in = Cache.load(key);
			} else {
				in = Cache.copy(url.toString(), url.openStream());
			}
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringWriter w = new StringWriter();
			Utils.readFully(r, w);
			r.close();
			w.close();
			String ret = w.toString();
			JSONObject jsonRet = new JSONObject(ret);
			String error;
			if ((error = jsonRet.optString("error")) != "") {
				throw new Exception(error);
			}
			JSONArray jsonResources = jsonRet.getJSONArray("resources");
			List<Resource> list = new ArrayList<Resource>();
			int imgCount = 0;
			for (int i = 0; i < jsonResources.length(); i++) {
				Resource res = Resource.fromJSONObject(jsonResources
						.getJSONObject(i));
				if (Hilos.isDebug()) {
					if ((Resource.isImageType(res.resourceType) && ++imgCount > 3)
							&& !Resource.isAudioType(res.resourceType)
							&& !Resource.isVideoType(res.resourceType)) {
						continue;
					}
				}
				if (filter.accept(res)) {
					list.add(res);
				}
			}
			resources = list.toArray(new Resource[0]);
		} catch (Exception e) {
			e.printStackTrace();
			resources = new Resource[0];
		}
		return resources;
	}

	public static InputStream getResourceInputStream(int id,
			String resourceType, boolean forceRefresh) {
		try {
			System.err.println("downloading: " + id);
			URL url = new URL(getResourceURL(id, resourceType));
			String key = url.toString();
			if (Cache.exists(key) && !forceRefresh) {
				System.err.println("... found in cache");
				return Cache.load(key);
			}
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("Resource not found!");
			}
			InputStream in = Cache.copy(url.toString(), httpConn.getInputStream());
			System.err.println("... done");
			return in;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getResourceURL(int id, String resourceType) {
		return Hilos.getHostname() + "/getResource?id=" + id + "&resourceType="
				+ resourceType;
	}

	public static String[] getStyleItemList(int style) {
		try {
			URL url = new URL(Hilos.getHostname() + "/getStyleItemList?style="
					+ style);
			InputStream in = url.openStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringWriter w = new StringWriter();
			Utils.readFully(r, w);
			r.close();
			w.close();
			String ret = w.toString();
			JSONObject jsonRet = new JSONObject(ret);
			String error;
			if ((error = jsonRet.optString("error")) != "") {
				throw new Exception(error);
			}
			JSONArray jsonStyleItems = jsonRet.getJSONArray("styleItems");
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < jsonStyleItems.length(); i++) {
				String res = jsonStyleItems.getString(i);
				list.add(res);
			}
			return list.toArray(new String[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream getStyleItemInputStream(int style, String name) {
		try {
			URL url = new URL(getStyleItemURL(style, name));
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("StyleItem not found!");
			}
			return httpConn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getStyleItemURL(int style, String name) {
		return Hilos.getHostname() + "/getStyleItem?style=" + style + "&name="
				+ name;
	}

	public static Model[] getModelList(boolean forceRefresh) {
		Model[] models;
		try {
			String lang = Locale.getDefault().getLanguage();
			URL url = new URL(Hilos.getHostname() + "/getModelList?lang="
					+ lang);
			InputStream in;
			String key = url.toString();
			if (Cache.exists(key) && !forceRefresh) {
				in = Cache.load(key);
			} else {
				in = Cache.copy(url.toString(), url.openStream());
			}
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringWriter w = new StringWriter();
			Utils.readFully(r, w);
			r.close();
			w.close();
			String ret = w.toString();
			JSONObject jsonRet = new JSONObject(ret);
			String error;
			if ((error = jsonRet.optString("error")) != "") {
				throw new Exception(error);
			}
			JSONArray jsonModels = jsonRet.getJSONArray("models");
			List<Model> list = new ArrayList<Model>();
			for (int i = 0; i < jsonModels.length(); i++) {
				Model res = Model.fromJSON(jsonModels.getJSONObject(i));
				list.add(res);
			}
			models = list.toArray(new Model[0]);
		} catch (Exception e) {
			e.printStackTrace();
			models = new Model[0];
		}
		return models;
	}

	public static InputStream getModelInputStream(String name,
			boolean forceRefresh) {
		try {
			URL url = new URL(getModelURL(name));
			String key = url.toString();
			if (Cache.exists(key) && !forceRefresh) {
				return Cache.load(key);
			}
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();
			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("Model not found!");
			}
			return Cache.copy(url.toString(), httpConn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getModelURL(String name) {
		return Hilos.getHostname() + "/getModel?name=" + name;
	}
}

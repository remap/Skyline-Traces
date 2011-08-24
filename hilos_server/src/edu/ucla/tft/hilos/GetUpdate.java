package edu.ucla.tft.hilos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A servlet to downloaded an updated version of the application.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class GetUpdate extends HttpServlet {
	private static final int GET_LATEST_VERSION_NUMBER = 0,
			DOWNLOAD_UPDATE = 1;
	private File updatesDir;
	private int latestVersion;
	private File updateFile;

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader fr = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(fr);
			fr.close();
			File dir;
			if (!(dir = new File(properties.getProperty("updatesDir")))
					.isDirectory()) {
				throw new Exception(
						"Invalid updatesDir in 'server.properties': " + dir);
			}
			String apkName;
			if ((apkName = properties.getProperty("apkName")) == null) {
				throw new Exception("Invalid apkName in 'server.properties': "
						+ dir);
			}
			updatesDir = dir;
			BufferedReader br = new BufferedReader(new FileReader(new File(
					updatesDir + "/latestVersion.txt")));
			latestVersion = Integer.parseInt(br.readLine());
			updateFile = new File(updatesDir + "/" + latestVersion + "/"
					+ apkName);
			br.close();
			if (!updateFile.exists()) {
				throw new Exception("File does not exist: " + updateFile);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String action;
		if ((action = req.getParameter("a")) == null) {
			throw new ServletException("Missing parameter: 'a'");
		}
		switch (Integer.parseInt(action)) {
		case GET_LATEST_VERSION_NUMBER:
			resp.setContentType("text/plain");
			PrintWriter w = null;
			try {
				JSONObject ret = new JSONObject();
				w = resp.getWriter();
				ret.put("latestVersion", latestVersion);
				ret.put("fileSize", updateFile.length());
				ret.write(w);
			} catch (Exception e) {
				if (w == null) {
					w = resp.getWriter();
				}
				try {
					JSONObject ret = new JSONObject();
					ret.put("error", e.getMessage());
					ret.write(w);
				} catch (JSONException je) {
					// shouldn't happen
				}
			}
			if (w != null) {
				w.close();
			}
			break;
		case DOWNLOAD_UPDATE:
			resp.setContentType("application/octet-stream");
			OutputStream out = null;
			try {
				out = resp.getOutputStream();
				InputStream in = new FileInputStream(updateFile);
				Utils.readFully(in, out);
			} catch (Exception e) {
				throw new ServletException(e);
			}
			if (out != null) {
				out.close();
			}
			break;
		}
	}
}

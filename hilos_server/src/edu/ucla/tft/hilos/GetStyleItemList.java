package edu.ucla.tft.hilos;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Returns the list of all style items for a particular style.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class GetStyleItemList extends HttpServlet {
	private File mStyleDirRoot;

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader r = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(r);
			r.close();
			File dir;
			if (!(dir = new File(properties.getProperty("styleDir")))
					.isDirectory()) {
				throw new Exception("Invalid styleDir in 'server.properties': "
						+ dir);
			} else {
				mStyleDirRoot = dir;
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		handleRequest(req, resp);
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		try {
			String style = req.getParameter("style");
			File dir;
			if (style == null
					|| !(dir = new File(mStyleDirRoot + "/" + style)).exists()) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				JSONObject ret = new JSONObject();
				try {
					JSONArray resources = new JSONArray();
					for (File f : dir.listFiles()) {
						resources.put(f.getName());
					}
					ret.put("styleItems", resources);
				} catch (Exception e) {
					e.printStackTrace();
					ret.put("error", Constants.s[Constants.DATABASE_ERROR]);
				}
				resp.setContentType("text/plain");
				PrintWriter pw = resp.getWriter();
				ret.write(pw);
				pw.close();
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}

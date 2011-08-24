package edu.ucla.tft.hilos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns a style item (images and other assets used in the user interface),
 * given a style index and the item's name.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class GetStyleItem extends HttpServlet {
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
			String name = req.getParameter("name");
			File file;
			if (style == null
					|| name == null
					|| !(file = new File(mStyleDirRoot + "/" + style + "/"
							+ name)).exists()) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.setContentLength((int) file.length());
				InputStream in = new FileInputStream(file);
				OutputStream out = resp.getOutputStream();
				Utils.readFully(in, out);
				in.close();
				out.close();
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}

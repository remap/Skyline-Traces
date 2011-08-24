package edu.ucla.tft.hilos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns a custom tile in a particular style, if available, otherwise returns
 * a tile from <a
 * href="http://tile.openstreetmap.org">http://tile.openstreetmap.org</a>.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class GetTile extends HttpServlet {
	private File mTilesDir;

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader r = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(r);
			r.close();
			File dir;
			if (!(dir = new File(properties.getProperty("tilesDir")))
					.isDirectory()) {
				throw new Exception("Invalid tilesDir in 'server.properties': "
						+ dir);
			} else {
				mTilesDir = dir;
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
			// Style index
			String si = req.getParameter("si");
			if (si == null) {
				si = "0";
			}
			String rp = req.getParameter("rp");
			if (rp == null) {
				throw new ServletException("Missing parameter: rp");
			}
			if (!rp.startsWith("/")) {
				rp = "/" + rp;
			}
			File file = new File(mTilesDir + "/" + si + rp);
			if (!file.exists()) {
				// download from openstreetmap
				InputStream in = new URL("http://tile.openstreetmap.org" + rp)
						.openStream();
				File dir = file.getParentFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
				OutputStream out = new FileOutputStream(file);
				Utils.readFully(in, out);
				in.close();
				out.close();
			}
			resp.setContentType("image/png");
			InputStream in = new FileInputStream(file);
			OutputStream out = resp.getOutputStream();
			Utils.readFully(in, out);
			in.close();
			out.close();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}

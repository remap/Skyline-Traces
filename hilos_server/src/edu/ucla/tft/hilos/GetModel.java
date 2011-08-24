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
 * Returns the 3D model file (along with related assets) with a given name.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class GetModel extends HttpServlet {
	private File mModelDir;

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader r = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(r);
			r.close();
			File dir;
			if (!(dir = new File(properties.getProperty("modelDir")))
					.isDirectory()) {
				throw new Exception("Invalid modelDir in 'server.properties': "
						+ dir);
			} else {
				mModelDir = dir;
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		handleRequest(req, resp);
	}

	private String transformExtension(String resourceID) {
		int n = resourceID.indexOf(':') + 1;
		String packageID = resourceID.substring(0, n);
		String name = resourceID.substring(n);
		int index = name.lastIndexOf('.');
		if (index >= 0) {
			name = name.substring(0, index) + "_" + name.substring(index + 1);
		}
		resourceID = packageID + name;
		return resourceID;
	}

	private String appendExtension(String resourceID) {
		int n = resourceID.indexOf(':') + 1;
		String packageID = resourceID.substring(0, n);
		String name = resourceID.substring(n);
		int index = name.lastIndexOf('.');
		if (index >= 0) {
			name = name.substring(0, index) + "_" + name.substring(index + 1);
		}
		resourceID = packageID + name;
		return resourceID;
	}

	private static final String[] sExts = { "png", "jpg", "jpeg", "gif", "obj",
			"mtl" };

	private File findFile(String name) {
		File file;
		if ((file = new File(mModelDir + "/" + name)).exists()) {
			return file;
		}
		for (String ext : sExts) {
			if ((file = new File(mModelDir + "/" + name + "." + ext)).exists()) {
				return file;
			}
		}
		for (String ext : sExts) {
			if (name.endsWith("_" + ext)) {
				if ((file = new File(mModelDir + "/"
						+ name.substring(0, name.lastIndexOf("_")) + "." + ext))
						.exists()) {
					return file;
				}
			}
		}
		return null;
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		try {
			String name = req.getParameter("name");
			File file = null;
			if (name == null || (file = findFile(name)) == null) {
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

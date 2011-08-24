package edu.ucla.tft.hilos;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetSkyline extends HttpServlet {
	private File mSkylineDir;

	public static BufferedImage createScaled(BufferedImage bitmap, int height) {
		int imgWidth = bitmap.getWidth();
		int imgHeight = bitmap.getHeight();
		if (imgHeight == height) {
			return bitmap;
		}
		double scale = (double) height / imgHeight;
		int scaledWidth = (int) (imgWidth * scale);
		int scaledHeight = height;
		BufferedImage out = new BufferedImage(scaledWidth, scaledHeight,
				bitmap.getType());
		Graphics2D g = out.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(bitmap, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return out;
	}

	private File getImageFile(String si, int height) throws Exception {
		File file = new File(mSkylineDir + "/" + si + "_" + height + ".png");
		if (!file.exists()) {
			File origImage = new File(mSkylineDir + "/" + si + ".png");
			if (!origImage.exists()) {
				return null;
			}
			ImageIO.write(createScaled(ImageIO.read(origImage), height), "png",
					file);
		}
		return file;
	}

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader r = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(r);
			r.close();
			File dir;
			if (!(dir = new File(properties.getProperty("skylineDir")))
					.isDirectory()) {
				throw new Exception(
						"Invalid skylineDir in 'server.properties': " + dir);
			} else {
				mSkylineDir = dir;
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
			int height = 0;
			String height_str = req.getParameter("height");
			if (height_str == null) {
				throw new ServletException("Missing parameter: height");
			} else {
				try {
					height = Integer.parseInt(height_str);
				} catch (NumberFormatException e) {
					throw new ServletException("Invalid parameter: height");
				}
			}
			File file;
			if ((file = getImageFile(si, height)) == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				resp.setContentType("image/png");
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

package edu.ucla.tft.hilos;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns a resource with a given <code>id</code> of a particular
 * <code>resourceType</code>. Usually, just fetches content from the
 * corresponding Gallery2 web interface. For a JPEG image, additional
 * pre-processing is done: Resize to less than a maximum size and then cache it
 * on disk.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class GetResource extends HttpServlet {
	private Map<String, File> mResourceDirMap;
	private Map<String, String> mResourceMimeTypeMap;
	private String downloadResourceURL;

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader r = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(r);
			r.close();
			downloadResourceURL = properties.getProperty("downloadResourceURL");
			mResourceDirMap = new HashMap<String, File>();
			File dir;
			if (!(dir = new File(properties.getProperty("imageDir")))
					.isDirectory()) {
				throw new Exception("Invalid imageDir in 'server.properties': "
						+ dir);
			} else {
				mResourceDirMap.put(Resource.RESOURCE_TYPE_IMAGE_JPG, dir);
				mResourceDirMap.put(Resource.RESOURCE_TYPE_IMAGE_PNG, dir);
				mResourceDirMap.put(Resource.RESOURCE_TYPE_IMAGE_GIF, dir);
			}
			if (!(dir = new File(properties.getProperty("audioDir")))
					.isDirectory()) {
				throw new Exception("Invalid audioDir in 'server.properties': "
						+ dir);
			} else {
				mResourceDirMap.put(Resource.RESOURCE_TYPE_AUDIO_MP3, dir);
			}
			if (!(dir = new File(properties.getProperty("videoDir")))
					.isDirectory()) {
				throw new Exception("Invalid videoDir in 'server.properties': "
						+ dir);
			} else {
				mResourceDirMap.put(Resource.RESOURCE_TYPE_VIDEO_MP4, dir);
			}
			mResourceMimeTypeMap = new HashMap<String, String>();
			mResourceMimeTypeMap.put(Resource.RESOURCE_TYPE_IMAGE_GIF,
					"image/gif");
			mResourceMimeTypeMap.put(Resource.RESOURCE_TYPE_IMAGE_JPG,
					"image/jpeg");
			mResourceMimeTypeMap.put(Resource.RESOURCE_TYPE_IMAGE_PNG,
					"image/png");
			mResourceMimeTypeMap.put(Resource.RESOURCE_TYPE_AUDIO_MP3,
					"audio/mpeg");
			mResourceMimeTypeMap.put(Resource.RESOURCE_TYPE_VIDEO_MP4,
					"video/mpeg");
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
			String id = req.getParameter("id");
			String resourceType = req.getParameter("resourceType");
			InputStream in;
			if (id == null || resourceType == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			} else if (resourceType.equals("jpg")) {
				// cache and resize jpg images
				File file;
				if (!(file = new File(mResourceDirMap.get(resourceType) + "/"
						+ id + "." + resourceType)).exists()) {
					in = new URL(String.format(downloadResourceURL, id))
							.openStream();
					OutputStream out = new FileOutputStream(file);
					Utils.readFully(in, out);
					in.close();
					out.close();
					resizeImage(file);
				}
				in = new FileInputStream(file);
			} else {
				in = new URL(String.format(downloadResourceURL, id))
						.openStream();
			}
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType(mResourceMimeTypeMap.get(resourceType));
			OutputStream out = resp.getOutputStream();
			Utils.readFully(in, out);
			in.close();
			out.close();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void resizeImage(File file) throws IOException {
		double maxSize = 600;
		BufferedImage img = ImageIO.read(file);
		String name = file.getName();
		String ext = name.substring(name.lastIndexOf('.') + 1);
		double scale = Math.min(maxSize / img.getWidth(),
				maxSize / img.getHeight());
		if (scale >= 1) {
			return;
		}
		BufferedImage res = new BufferedImage((int) (scale * img.getWidth()),
				(int) (scale * img.getHeight()), img.getType());
		Graphics2D g = res.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(img, 0, 0, res.getWidth(), res.getHeight(), null);
		ImageIO.write(res, ext, file);
	}
}

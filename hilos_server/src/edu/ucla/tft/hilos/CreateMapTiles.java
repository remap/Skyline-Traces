package edu.ucla.tft.hilos;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Provides a web service to create map tiles out of a monolithic image. The
 * provided image must of of size 2200x1400 pixels (see <a href=
 * "http://lashp.remap.ucla.edu/visual/main.php?g2_view=core.DownloadItem&g2_itemId=519"
 * >this base map image</a>) for reference)
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class CreateMapTiles extends HttpServlet {
	private static final int STATUS_IDLE = 0, STATUS_PROCESSING = 1,
			STATUS_DONE = 2, STATUS_ERROR = 3;
	private File tempDir;
	private int status;

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader r = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(r);
			r.close();
			File dir;
			if (!(dir = new File(properties.getProperty("tempDir")))
					.isDirectory()) {
				throw new Exception("Invalid tempDir in 'server.properties': "
						+ dir);
			} else {
				tempDir = dir;
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		try {
			String a = req.getParameter("a");
			if ("status".equalsIgnoreCase(a)) {
				resp.setContentType("text/html");
				PrintWriter w = resp.getWriter();
				switch (status) {
				case STATUS_IDLE:
					w.println("Idle");
					break;
				case STATUS_PROCESSING:
					w.println("Processing... please refresh again in a few moments.");
					break;
				case STATUS_DONE:
					w.println("Done. <a href=\"?a=download\">Download tiles</a>.");
					status = STATUS_IDLE;
					break;
				case STATUS_ERROR:
					w.println("Error.");
					status = STATUS_IDLE;
					break;
				}
				w.close();
			} else if ("download".equalsIgnoreCase(a)) {
				resp.setContentType("application/zip");
				resp.setHeader("Content-Disposition",
						"attachment; filename=\"tiles.zip\"");
				FileInputStream in = new FileInputStream(tempDir + "/tiles.zip");
				OutputStream out = resp.getOutputStream();
				Utils.readFully(in, out);
				in.close();
				out.close();
			} else {
				resp.setContentType("text/html");
				PrintWriter w = resp.getWriter();
				w.println("<html><head><title>Create Map Tiles</title></head>");
				w.println("<body><form action=\"createMapTiles\" method=\"POST\" enctype=\"multipart/form-data\">Map Image: <input type=\"file\" name=\"map\"><br>Background Image: <input type=\"file\" name=\"background\"><br><input type=\"submit\" value=\"Create\"></form></body>");
				w.println("</html>");
				w.close();
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		handleRequest(req, resp);
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		try {
			if (status == STATUS_PROCESSING) {
				PrintWriter w = resp.getWriter();
				w.println("Processing a request... please try again later.");
				w.close();
				return;
			}
			status = STATUS_PROCESSING;
			DiskFileItemFactory f = new DiskFileItemFactory();
			f.setSizeThreshold(50 * 1024 * 1024);
			Utils.empty(tempDir);
			f.setRepository(tempDir);
			ServletFileUpload handler = new ServletFileUpload(f);
			List<FileItem> items = handler.parseRequest(req);
			File mapFile = null;
			File backgroundFile = null;
			for (FileItem item : items) {
				String name = item.getFieldName();
				if (item.isFormField()) {
					continue;
				}
				if ("map".equals(name)) {
					mapFile = new File(tempDir + "/map.png");
					item.write(mapFile);
				} else if ("background".equals(name)) {
					backgroundFile = new File(tempDir + "/background.png");
					item.write(backgroundFile);
				}
			}
			if (mapFile == null || backgroundFile == null) {
				throw new Exception("Invalid request");
			}
			resp.setContentType("text/html");
			PrintWriter w = resp.getWriter();
			w.println("<a href=\"?a=status\">Check status</a>");
			w.close();
			new CreateMapThread(mapFile, backgroundFile).start();
		} catch (Exception e) {
			status = STATUS_ERROR;
			throw new ServletException(e);
		}
	}

	private class CreateMapThread extends Thread {
		private File mapFile;
		private File backgroundFile;

		public CreateMapThread(File mapFile, File backgroundFile) {
			this.mapFile = mapFile;
			this.backgroundFile = backgroundFile;
		}

		@Override
		public void run() {
			try {
				BufferedImage map = ImageIO.read(mapFile);
				BufferedImage bgTex = ImageIO.read(backgroundFile);
				double enlarge = 1.6;
				double angle = Math.toRadians(-26.1);
				int centerX = 2806, centerY = 2356;
				BufferedImage adjusted = new BufferedImage(5120, 5120,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = adjusted.createGraphics();
				g.translate(centerX, centerY);
				g.rotate(angle);
				g.scale(enlarge, enlarge);
				int nBgTexTiles = adjusted.getWidth()
						/ Math.min(bgTex.getWidth(), bgTex.getHeight()) + 1;
				for (int y = -nBgTexTiles * bgTex.getHeight() / 2; y < nBgTexTiles
						* bgTex.getHeight() / 2; y += bgTex.getHeight()) {
					for (int x = -nBgTexTiles * bgTex.getWidth() / 2; x < nBgTexTiles
							* bgTex.getWidth() / 2; x += bgTex.getWidth()) {
						g.drawImage(bgTex, x, y, null);
					}
				}
				g.drawImage(map, -map.getWidth() / 2, -map.getHeight() / 2,
						null);
				g.dispose();
				ImageIO.write(adjusted, "png", new File(mapFile.getParent()
						+ "/adjusted.png"));
				for (int zoom = 16; zoom <= 18; zoom++) {
					BufferedImage in = scaleDown(adjusted, 0x1 << (18 - zoom));
					int m = 0x1 << (zoom - 16);
					int sx = 11242 * m;
					int sy = 26162 * m;
					BufferedImage out = new BufferedImage(256, 256,
							BufferedImage.TYPE_INT_RGB);
					g = out.createGraphics();
					for (int i = 0; i < 5 * m; i++) {
						for (int j = 0; j < 5 * m; j++) {
							System.err.println(i + "," + j);
							File outDir = new File(mapFile.getParent()
									+ "/tiles/" + zoom + "/" + (sx + i));
							if (!outDir.exists()) {
								outDir.mkdirs();
							}
							File file = new File(outDir + "/" + (sy + j)
									+ ".png");
							System.err.println(file + ": " + file.exists());
							if (file.exists()) {
								continue;
							}
							g.drawImage(in, -i * 256, -j * 256, null);
							ImageIO.write(out, "png", file);
						}
					}
					in = null;
				}
				ZipOutputStream zout = new ZipOutputStream(
						new FileOutputStream(mapFile.getParent() + "/tiles.zip"));
				createZip(new File(mapFile.getParent() + "/tiles"), zout,
						"tiles");
				zout.close();
				status = STATUS_DONE;
			} catch (Exception e) {
				e.printStackTrace();
				status = STATUS_ERROR;
			}
		}

		private void createZip(File dir, ZipOutputStream zout, String prefix)
				throws Exception {
			for (File f : dir.listFiles()) {
				if (f.isDirectory()) {
					createZip(f, zout, prefix + "/" + f.getName());
				} else if (f.isFile()) {
					zout.putNextEntry(new ZipEntry(prefix + "/" + f.getName()));
					InputStream in = new FileInputStream(f);
					Utils.readFully(in, zout);
					in.close();
					zout.closeEntry();
				}
			}
		}
	}

	private static BufferedImage scaleDown(BufferedImage src, int times) {
		if (times == 1) {
			return src;
		}
		BufferedImage dest = new BufferedImage(src.getWidth() / times,
				src.getHeight() / times, src.getType());
		Graphics2D g = dest.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(src, 0, 0, dest.getWidth(), dest.getHeight(), null);
		g.dispose();
		return dest;
	}
}

package edu.ucla.tft.hilos;

import static edu.ucla.tft.hilos.Utils.TR;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.tft.hilos.Scene.Model;
import edu.ucla.tft.hilos.Scene.Vec2;
import edu.ucla.tft.hilos.Scene.Vec3;

/**
 * Returns a list of all models in currently in the database.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class GetModelList extends HttpServlet {
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		handleRequest(req, resp);
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		try {
			String lang = req.getParameter("lang");
			JSONObject ret = new JSONObject();
			Connection conn = null;
			Statement stmt = null;
			ResultSet set = null;
			try {
				conn = Db.getConnection("hilos");
				stmt = conn.createStatement();
				String query = "select * from models";
				set = stmt.executeQuery(query);
				JSONArray models = new JSONArray();
				while (set.next()) {
					String src = set.getString("src");
					String title = TR(conn, set.getString("title"), lang);
					double latitude = set.getDouble("latitude");
					double longitude = set.getDouble("longitude");
					Vec2 location = new Vec2((float) latitude,
							(float) longitude);
					String[] scale_str = set.getString("scale").split(",");
					Vec3 scale = new Vec3(Float.parseFloat(scale_str[0]),
							Float.parseFloat(scale_str[1]),
							Float.parseFloat(scale_str[2]));
					String[] anchor_str = set.getString("anchor").split(",");
					Vec3 anchor = new Vec3(Float.parseFloat(anchor_str[0]),
							Float.parseFloat(anchor_str[1]),
							Float.parseFloat(anchor_str[2]));
					Model model = new Model(src, title, location, scale, anchor);
					models.put(model.toJSON());
				}
				ret.put("models", models);
			} catch (Exception e) {
				e.printStackTrace();
				ret.put("error", Constants.s[Constants.DATABASE_ERROR]);
			} finally {
				Utils.close(set, stmt, conn);
			}
			resp.setContentType("text/plain");
			PrintWriter pw = resp.getWriter();
			ret.write(pw);
			pw.close();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}

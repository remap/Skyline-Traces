package edu.ucla.tft.hilos;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet handling email verification requests.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class VerifyEmail extends HttpServlet {
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req, resp);
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String code = req.getParameter("c");
		if (code == null) {
			resp.setContentType("text/plain");
			try {
				JSONObject json = new JSONObject();
				json.put("error", Constants.s[Constants.INVALID_REQUEST]);
				PrintWriter pw = resp.getWriter();
				json.write(pw);
				pw.close();
			} catch (JSONException e) {
				// shouldn't come here
			}
			return;
		}
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		String error = null;
		try {
			conn = Db.getConnection("hilos");
			stmt = conn.createStatement();
			String query = "select email from verify where code='" + code + "'";
			set = stmt.executeQuery(query);
			if (set.next()) {
				String email = set.getString("email");
				query = "update users set status=" + Constants.VERIFIED
						+ " where email='" + email + "'";
				stmt.executeUpdate(query);
				query = "delete from verify where code='" + code + "'";
				stmt.executeUpdate(query);
			} else {
				error = Constants.s[Constants.INVALID_REQUEST];
			}
		} catch (Exception e) {
			e.printStackTrace();
			error = Constants.s[Constants.DATABASE_ERROR];
		} finally {
			Utils.close(set, stmt, conn);
		}
		resp.setContentType("text/plain");
		try {
			JSONObject json = new JSONObject();
			if (error == null) {
				json.put("return", Constants.s[Constants.OK]);
			} else {
				json.put("error", error);
			}
			PrintWriter pw = resp.getWriter();
			json.write(pw);
			pw.close();
		} catch (JSONException e) {
			// shouldn't come here
		}
	}
}

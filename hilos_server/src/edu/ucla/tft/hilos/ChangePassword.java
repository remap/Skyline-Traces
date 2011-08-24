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
 * A servlet which handles changing the password of a user.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class ChangePassword extends HttpServlet {
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleLogin(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleLogin(req, resp);
	}

	private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String uid = req.getParameter("uid");
		String current_password = req.getParameter("current_password");
		String new_password = req.getParameter("new_password");
		if (uid == null || current_password == null || new_password == null) {
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
			String query = "select * from users where uid=" + uid
					+ " and password='" + current_password + "'";
			set = stmt.executeQuery(query);
			if (set.next()) {
				stmt = conn.createStatement();
				query = "update users set password='" + new_password
						+ "' where uid=" + uid;
				stmt.executeUpdate(query);
			} else {
				error = Constants.s[Constants.INVALID_LOGIN];
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

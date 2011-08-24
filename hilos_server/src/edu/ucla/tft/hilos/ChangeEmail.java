package edu.ucla.tft.hilos;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A servlet which handles changing the email address of a user.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class ChangeEmail extends HttpServlet {
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
		String uid = req.getParameter("uid");
		String email = req.getParameter("email");
		if (uid == null || email == null) {
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
		String error = null;
		try {
			conn = Db.getConnection("hilos");
			stmt = conn.createStatement();
			String query = "update users set email='" + email + "' where uid="
					+ uid;
			stmt.executeUpdate(query);
		} catch (Exception e) {
			e.printStackTrace();
			error = Constants.s[Constants.DATABASE_ERROR];
		} finally {
			Utils.close(stmt, conn);
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

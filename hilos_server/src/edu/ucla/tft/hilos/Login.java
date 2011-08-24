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
 * Supports authentication for users. Returns the user's UID if an entry is
 * found.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Login extends HttpServlet {
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
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		if (email == null || password == null) {
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
		int uid = 0;
		try {
			conn = Db.getConnection("hilos");
			stmt = conn.createStatement();
			String query = "select uid, email, password, status from users";
			set = stmt.executeQuery(query);
			while (set.next()) {
				String e = set.getString("email");
				String p = set.getString("password");
				int status = set.getInt("status");
				if (e.equals(email) && p.equals(password)
						&& status == Constants.VERIFIED) {
					uid = set.getInt("uid");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utils.close(set, stmt, conn);
		}
		resp.setContentType("text/plain");
		try {
			JSONObject json = new JSONObject();
			if (uid > 0) {
				json.put("return", Constants.s[Constants.OK]);
				json.put("uid", uid);
			} else {
				json.put("error", Constants.s[Constants.INVALID_LOGIN]);
			}
			PrintWriter pw = resp.getWriter();
			json.write(pw);
			pw.close();
		} catch (JSONException e) {
			// shouldn't come here
		}
	}
}

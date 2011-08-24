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
 * A servlet which resets the password of a user. Generates a random
 * 10-character password and sends an email to the user with the new password.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class ResetPassword extends HttpServlet {
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
		String lang = req.getParameter("lang");
		if (lang == null) {
			lang = "en";
		}
		if (email == null) {
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
		String new_password = null;
		try {
			conn = Db.getConnection("hilos");
			stmt = conn.createStatement();
			String query = "select * from users where email='" + email + "'";
			set = stmt.executeQuery(query);
			if (set.next()) {
				int uid = set.getInt("uid");
				String name = set.getString("name");
				new_password = Utils.randomAlphaNum(10);
				Utils.close(stmt);
				stmt = conn.createStatement();
				query = "update users set password='" + new_password
						+ "' where uid=" + uid;
				stmt.executeUpdate(query);
				EmailQueue.get().addItem(
						Email.resetPassword.create(getServletContext(), email,
								name, new_password, lang));
			} else {
				error = Constants.s[Constants.DOES_NOT_EXIST];
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

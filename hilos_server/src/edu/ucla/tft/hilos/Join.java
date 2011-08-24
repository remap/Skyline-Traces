package edu.ucla.tft.hilos;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A servlet which handles user registration.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class Join extends HttpServlet {
	private String hostname;

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader r = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(r);
			r.close();
			if ((hostname = properties.getProperty("hostname")) == null
					|| !hostname.startsWith("http://")) {
				throw new Exception("Invalid hostname in 'server.properties': "
						+ hostname);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

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
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		String name = req.getParameter("name");
		String lang = req.getParameter("lang");
		if (lang == null) {
			lang = "en";
		}
		if (email == null || password == null || name == null) {
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
			String query;
			conn = Db.getConnection("hilos");
			stmt = conn.createStatement();
			query = "select * from users where email='" + email + "'";
			if (stmt.executeQuery(query).next() == false) {
				String code = Utils.randomAlphaNum(30);
				query = "insert into users (email, name, password, status) values ('"
						+ email
						+ "','"
						+ name
						+ "','"
						+ password
						+ "',"
						+ Constants.UNVERIFIED + ")";
				stmt.executeUpdate(query);
				query = "insert into verify (code, email) values ('" + code
						+ "','" + email + "')";
				stmt.executeUpdate(query);
				String url = hostname + "/verifyEmail?c=" + code + "&lang="
						+ lang;
				EmailQueue.get().addItem(
						Email.verifyEmail.create(getServletContext(), email,
								name, url, lang));
			}
		} catch (Exception e) {
			error = Constants.s[Constants.DATABASE_ERROR];
			e.printStackTrace();
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

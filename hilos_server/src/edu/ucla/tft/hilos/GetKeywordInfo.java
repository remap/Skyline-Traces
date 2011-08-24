package edu.ucla.tft.hilos;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns an information token for a particular keyword, usually a URL to a
 * relevant page online.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class GetKeywordInfo extends HttpServlet {
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		handleRequest(req, resp);
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		try {
			conn = Db.getConnection("hilos");
			stmt = conn.createStatement();
			String lang = req.getParameter("lang");
			String keyword = req.getParameter("keyword").toLowerCase();
			String query = String
					.format("select info from keyword_info where keyword='%s'",
							keyword);
			set = stmt.executeQuery(query);
			String info = null;
			if (set.next()) {
				info = set.getString("info");
			}
			resp.setContentType("text/html");
			PrintWriter w = resp.getWriter();
			if (info == null || (info = info.trim()).length() == 0) {
				w.println("<html><body>No Information Available</body></html>");
			} else {
				w.print(info);
			}
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		} finally {
			Utils.close(set, stmt, conn);
		}
	}
}

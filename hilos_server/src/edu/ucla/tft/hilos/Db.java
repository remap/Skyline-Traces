package edu.ucla.tft.hilos;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Provides access to the underlying database connection pool.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Db {
	public static Connection getConnection(String dbname) throws SQLException {
		DataSource ds = null;
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:comp/env");
			ds = (DataSource) envContext.lookup("jdbc/" + dbname);
		} catch (NamingException e) {
			new SQLException(e);
		}
		return ds.getConnection();
	}
}

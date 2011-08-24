package edu.ucla.tft.hilos;

/**
 * Some commonly used constants.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class Constants {
	public static final int OK = 0;
	public static final int INVALID_REQUEST = 1;
	public static final int INVALID_LOGIN = 2;
	public static final int ALREADY_EXISTS = 3;
	public static final int INVALID_UID = 4;
	public static final int DATABASE_ERROR = 6;
	public static final int DOES_NOT_EXIST = 7;

	public static final int UNVERIFIED = 0, VERIFIED = 1;

	public static final String[] s;
	static {
		s = new String[10];
		s[OK] = "OK";
		s[INVALID_REQUEST] = "Invalid Request";
		s[INVALID_LOGIN] = "Invalid Login Credentials";
		s[ALREADY_EXISTS] = "User Already Exists";
		s[INVALID_UID] = "Invalid UID";
		s[DATABASE_ERROR] = "Database Error";
		s[DOES_NOT_EXIST] = "User Does Not Exist";
	}
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ucla.tft.hilos;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Perform email validations.
 * </p>
 * <p>
 * This class is a Singleton; you can retrieve the instance via the
 * getInstance() method.
 * </p>
 * <p>
 * Based on a script by <a href="mailto:stamhankar@hotmail.com">Sandeep V.
 * Tamhankar</a> http:
 * </p>
 * <p>
 * This implementation is not guaranteed to catch all possible errors in an
 * email address. For example, an address like nobody@noplace.somedog will pass
 * validator, even though there is no TLD "somedog"
 * </p>
 * .
 * 
 * @version $Revision$ $Date$
 * @since Validator 1.4
 */
public class EmailValidator implements Serializable {

	private static final String SPECIAL_CHARS = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";
	private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";
	private static final String QUOTED_USER = "(\"[^\"]*\")";
	private static final String WORD = "((" + VALID_CHARS + "|')+|"
			+ QUOTED_USER + ")";

	private static final String LEGAL_ASCII_REGEX = "^\\p{ASCII}+$";
	private static final String EMAIL_REGEX = "^\\s*?(.+)@(.+?)\\s*$";
	private static final String IP_DOMAIN_REGEX = "^\\[(.*)\\]$";
	private static final String USER_REGEX = "^\\s*" + WORD + "(\\." + WORD
			+ ")*$";

	private static final Pattern MATCH_ASCII_PATTERN = Pattern
			.compile(LEGAL_ASCII_REGEX);
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
	private static final Pattern IP_DOMAIN_PATTERN = Pattern
			.compile(IP_DOMAIN_REGEX);
	private static final Pattern USER_PATTERN = Pattern.compile(USER_REGEX);

	/**
	 * Singleton instance of this class.
	 */
	private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator();

	/**
	 * Returns the Singleton instance of this validator.
	 * 
	 * @return singleton instance of this validator.
	 */
	public static EmailValidator getInstance() {
		return EMAIL_VALIDATOR;
	}

	/**
	 * Protected constructor for subclasses to use.
	 */
	protected EmailValidator() {
		super();
	}

	/**
	 * <p>
	 * Checks if a field has a valid e-mail address.
	 * </p>
	 * 
	 * @param email
	 *            The value validation is being performed on. A
	 *            <code>null</code> value is considered invalid.
	 * @return true if the email address is valid.
	 */
	public boolean isValid(String email) {
		if (email == null) {
			return false;
		}

		Matcher asciiMatcher = MATCH_ASCII_PATTERN.matcher(email);
		if (!asciiMatcher.matches()) {
			return false;
		}

		Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
		if (!emailMatcher.matches()) {
			return false;
		}

		if (email.endsWith(".")) {
			return false;
		}

		if (!isValidUser(emailMatcher.group(1))) {
			return false;
		}

		if (!isValidDomain(emailMatcher.group(2))) {
			return false;
		}

		return true;
	}

	/**
	 * Returns true if the domain component of an email address is valid.
	 * 
	 * @param domain
	 *            being validated.
	 * @return true if the email address's domain is valid.
	 */
	protected boolean isValidDomain(String domain) {

		Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);

		if (ipDomainMatcher.matches()) {
			InetAddressValidator inetAddressValidator = InetAddressValidator
					.getInstance();
			return inetAddressValidator.isValid(ipDomainMatcher.group(1));
		} else {

			DomainValidator domainValidator = DomainValidator.getInstance();
			return domainValidator.isValid(domain);
		}
	}

	/**
	 * Returns true if the user component of an email address is valid.
	 * 
	 * @param user
	 *            being validated
	 * @return true if the user name is valid.
	 */
	protected boolean isValidUser(String user) {
		return USER_PATTERN.matcher(user).matches();
	}

	public static class DomainValidator implements Serializable {

		private static final String DOMAIN_LABEL_REGEX = "\\p{Alnum}(?>[\\p{Alnum}-]*\\p{Alnum})*";
		private static final String TOP_LABEL_REGEX = "\\p{Alpha}{2,}";
		private static final String DOMAIN_NAME_REGEX = "^(?:"
				+ DOMAIN_LABEL_REGEX + "\\.)+" + "(" + TOP_LABEL_REGEX + ")$";

		/**
		 * Singleton instance of this validator.
		 */
		private static final DomainValidator DOMAIN_VALIDATOR = new DomainValidator();

		/**
		 * RegexValidator for matching domains.
		 */
		private final RegexValidator domainRegex = new RegexValidator(
				DOMAIN_NAME_REGEX);

		/**
		 * Returns the singleton instance of this validator.
		 * 
		 * @return the singleton instance of this validator
		 */
		public static DomainValidator getInstance() {
			return DOMAIN_VALIDATOR;
		}

		/** Private constructor. */
		private DomainValidator() {
		}

		/**
		 * Returns true if the specified <code>String</code> parses as a valid
		 * domain name with a recognized top-level domain. The parsing is
		 * case-sensitive.
		 * 
		 * @param domain
		 *            the parameter to check for domain name syntax
		 * @return true if the parameter is a valid domain name
		 */
		public boolean isValid(String domain) {
			String[] groups = domainRegex.match(domain);
			if (groups != null && groups.length > 0) {
				return isValidTld(groups[0]);
			} else {
				return false;
			}
		}

		/**
		 * Returns true if the specified <code>String</code> matches any
		 * IANA-defined top-level domain. Leading dots are ignored if present.
		 * The search is case-sensitive.
		 * 
		 * @param tld
		 *            the parameter to check for TLD status
		 * @return true if the parameter is a TLD
		 */
		public boolean isValidTld(String tld) {
			return isValidInfrastructureTld(tld) || isValidGenericTld(tld)
					|| isValidCountryCodeTld(tld);
		}

		/**
		 * Returns true if the specified <code>String</code> matches any
		 * IANA-defined infrastructure top-level domain. Leading dots are
		 * ignored if present. The search is case-sensitive.
		 * 
		 * @param iTld
		 *            the parameter to check for infrastructure TLD status
		 * @return true if the parameter is an infrastructure TLD
		 */
		public boolean isValidInfrastructureTld(String iTld) {
			return INFRASTRUCTURE_TLD_LIST.contains(chompLeadingDot(iTld
					.toLowerCase()));
		}

		/**
		 * Returns true if the specified <code>String</code> matches any
		 * IANA-defined generic top-level domain. Leading dots are ignored if
		 * present. The search is case-sensitive.
		 * 
		 * @param gTld
		 *            the parameter to check for generic TLD status
		 * @return true if the parameter is a generic TLD
		 */
		public boolean isValidGenericTld(String gTld) {
			return GENERIC_TLD_LIST
					.contains(chompLeadingDot(gTld.toLowerCase()));
		}

		/**
		 * Returns true if the specified <code>String</code> matches any
		 * IANA-defined country code top-level domain. Leading dots are ignored
		 * if present. The search is case-sensitive.
		 * 
		 * @param ccTld
		 *            the parameter to check for country code TLD status
		 * @return true if the parameter is a country code TLD
		 */
		public boolean isValidCountryCodeTld(String ccTld) {
			return COUNTRY_CODE_TLD_LIST.contains(chompLeadingDot(ccTld
					.toLowerCase()));
		}

		private String chompLeadingDot(String str) {
			if (str.startsWith(".")) {
				return str.substring(1);
			} else {
				return str;
			}
		}

		private static final String[] INFRASTRUCTURE_TLDS = new String[] {
				"arpa", "root" };

		private static final String[] GENERIC_TLDS = new String[] { "aero",
				"asia", "biz", "cat", "com", "coop", "info", "jobs", "mobi",
				"museum", "name", "net", "org", "pro", "tel", "travel", "gov",
				"edu", "mil", "int" };

		private static final String[] COUNTRY_CODE_TLDS = new String[] { "ac",
				"ad", "ae", "af", "ag", "ai", "al", "am", "an", "ao", "aq",
				"ar", "as", "at", "au", "aw", "ax", "az", "ba", "bb", "bd",
				"be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br",
				"bs", "bt", "bv", "bw", "by", "bz", "ca", "cc", "cd", "cf",
				"cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cr", "cu",
				"cv", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz",
				"ec", "ee", "eg", "er", "es", "et", "eu", "fi", "fj", "fk",
				"fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", "gh",
				"gi", "gl", "gm", "gn", "gp", "gq", "gr", "gs", "gt", "gu",
				"gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie",
				"il", "im", "in", "io", "iq", "ir", "is", "it", "je", "jm",
				"jo", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr",
				"kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls",
				"lt", "lu", "lv", "ly", "ma", "mc", "md", "me", "mg", "mh",
				"mk", "ml", "mm", "mn", "mo", "mp", "mq", "mr", "ms", "mt",
				"mu", "mv", "mw", "mx", "my", "mz", "na", "nc", "ne", "nf",
				"ng", "ni", "nl", "no", "np", "nr", "nu", "nz", "om", "pa",
				"pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "ps",
				"pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa",
				"sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl",
				"sm", "sn", "so", "sr", "st", "su", "sv", "sy", "sz", "tc",
				"td", "tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to",
				"tp", "tr", "tt", "tv", "tw", "tz", "ua", "ug", "uk", "um",
				"us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn", "vu",
				"wf", "ws", "ye", "yt", "yu", "za", "zm", "zw", };

		private static final List INFRASTRUCTURE_TLD_LIST = Arrays
				.asList(INFRASTRUCTURE_TLDS);
		private static final List GENERIC_TLD_LIST = Arrays
				.asList(GENERIC_TLDS);
		private static final List COUNTRY_CODE_TLD_LIST = Arrays
				.asList(COUNTRY_CODE_TLDS);
	}

	/**
	 * <p>
	 * <b>InetAddress</b> validation and conversion routines (
	 * <code>java.net.InetAddress</code>).
	 * </p>
	 * 
	 * <p>
	 * This class provides methods to validate a candidate IP address.
	 * 
	 * <p>
	 * This class is a Singleton; you can retrieve the instance via the
	 * {@link #getInstance()} method.
	 * </p>
	 * 
	 * @version $Revision$
	 * @since Validator 1.4
	 */
	public static class InetAddressValidator implements Serializable {

		private static final String IPV4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";

		/**
		 * Singleton instance of this class.
		 */
		private static final InetAddressValidator VALIDATOR = new InetAddressValidator();

		/** IPv4 RegexValidator */
		private final RegexValidator ipv4Validator = new RegexValidator(
				IPV4_REGEX);

		/**
		 * Returns the singleton instance of this validator.
		 * 
		 * @return the singleton instance of this validator
		 */
		public static InetAddressValidator getInstance() {
			return VALIDATOR;
		}

		/**
		 * Checks if the specified string is a valid IP address.
		 * 
		 * @param inetAddress
		 *            the string to validate
		 * @return true if the string validates as an IP address
		 */
		public boolean isValid(String inetAddress) {
			return isValidInet4Address(inetAddress);
		}

		/**
		 * Validates an IPv4 address. Returns true if valid.
		 * 
		 * @param inet4Address
		 *            the IPv4 address to validate
		 * @return true if the argument contains a valid IPv4 address
		 */
		public boolean isValidInet4Address(String inet4Address) {
			// verify that address conforms to generic IPv4 format
			String[] groups = ipv4Validator.match(inet4Address);

			if (groups == null)
				return false;

			// verify that address subgroups are legal
			for (int i = 0; i <= 3; i++) {
				String ipSegment = groups[i];
				if (ipSegment == null || ipSegment.length() <= 0) {
					return false;
				}

				int iIpSegment = 0;

				try {
					iIpSegment = Integer.parseInt(ipSegment);
				} catch (NumberFormatException e) {
					return false;
				}

				if (iIpSegment > 255) {
					return false;
				}

			}

			return true;
		}
	}

	/**
	 * <b>Regular Expression</b> validation (using JDK 1.4+ regex support).
	 * <p>
	 * Construct the validator either for a single regular expression or a set
	 * (array) of regular expressions. By default validation is <i>case
	 * sensitive</i> but constructors are provided to allow <i>case
	 * in-sensitive</i> validation. For example to create a validator which does
	 * <i>case in-sensitive</i> validation for a set of regular expressions:
	 * 
	 * <pre>
	 *         String[] regexs = new String[] {...};
	 *         RegexValidator validator = new RegexValidator(regexs, false);
	 * </pre>
	 * <p>
	 * <ul>
	 * <li>Validate <code>true</code> or <code>false</code>:</li>
	 * <ul>
	 * <li><code>boolean valid = validator.isValid(value);</code></li>
	 * </ul>
	 * <li>Validate returning an aggregated String of the matched groups:</li>
	 * <ul>
	 * <li><code>String result = validator.validate(value);</code></li>
	 * </ul>
	 * <li>Validate returning the matched groups:</li>
	 * <ul>
	 * <li><code>String[] result = validator.match(value);</code></li>
	 * </ul>
	 * </ul>
	 * <p>
	 * Cached instances pre-compile and re-use {@link Pattern}(s) - which
	 * according to the {@link Pattern} API are safe to use in a multi-threaded
	 * environment.
	 * 
	 * @version $Revision$ $Date$
	 * @since Validator 1.4
	 */
	public static class RegexValidator implements Serializable {

		private final Pattern[] patterns;

		/**
		 * Construct a <i>case sensitive</i> validator for a single regular
		 * expression.
		 * 
		 * @param regex
		 *            The regular expression this validator will validate
		 *            against
		 */
		public RegexValidator(String regex) {
			this(regex, true);
		}

		/**
		 * Construct a validator for a single regular expression with the
		 * specified case sensitivity.
		 * 
		 * @param regex
		 *            The regular expression this validator will validate
		 *            against
		 * @param caseSensitive
		 *            when <code>true</code> matching is <i>case sensitive</i>,
		 *            otherwise matching is <i>case in-sensitive</i>
		 */
		public RegexValidator(String regex, boolean caseSensitive) {
			this(new String[] { regex }, caseSensitive);
		}

		/**
		 * Construct a <i>case sensitive</i> validator that matches any one of
		 * the set of regular expressions.
		 * 
		 * @param regexs
		 *            The set of regular expressions this validator will
		 *            validate against
		 */
		public RegexValidator(String[] regexs) {
			this(regexs, true);
		}

		/**
		 * Construct a validator that matches any one of the set of regular
		 * expressions with the specified case sensitivity.
		 * 
		 * @param regexs
		 *            The set of regular expressions this validator will
		 *            validate against
		 * @param caseSensitive
		 *            when <code>true</code> matching is <i>case sensitive</i>,
		 *            otherwise matching is <i>case in-sensitive</i>
		 */
		public RegexValidator(String[] regexs, boolean caseSensitive) {
			if (regexs == null || regexs.length == 0) {
				throw new IllegalArgumentException(
						"Regular expressions are missing");
			}
			patterns = new Pattern[regexs.length];
			int flags = (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
			for (int i = 0; i < regexs.length; i++) {
				if (regexs[i] == null || regexs[i].length() == 0) {
					throw new IllegalArgumentException("Regular expression["
							+ i + "] is missing");
				}
				patterns[i] = Pattern.compile(regexs[i], flags);
			}
		}

		/**
		 * Validate a value against the set of regular expressions.
		 * 
		 * @param value
		 *            The value to validate.
		 * @return <code>true</code> if the value is valid otherwise
		 *         <code>false</code>.
		 */
		public boolean isValid(String value) {
			if (value == null) {
				return false;
			}
			for (int i = 0; i < patterns.length; i++) {
				if (patterns[i].matcher(value).matches()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Validate a value against the set of regular expressions returning the
		 * array of matched groups.
		 * 
		 * @param value
		 *            The value to validate.
		 * @return String array of the <i>groups</i> matched if valid or
		 *         <code>null</code> if invalid
		 */
		public String[] match(String value) {
			if (value == null) {
				return null;
			}
			for (int i = 0; i < patterns.length; i++) {
				Matcher matcher = patterns[i].matcher(value);
				if (matcher.matches()) {
					int count = matcher.groupCount();
					String[] groups = new String[count];
					for (int j = 0; j < count; j++) {
						groups[j] = matcher.group(j + 1);
					}
					return groups;
				}
			}
			return null;
		}

		/**
		 * Validate a value against the set of regular expressions returning a
		 * String value of the aggregated groups.
		 * 
		 * @param value
		 *            The value to validate.
		 * @return Aggregated String value comprised of the <i>groups</i>
		 *         matched if valid or <code>null</code> if invalid
		 */
		public String validate(String value) {
			if (value == null) {
				return null;
			}
			for (int i = 0; i < patterns.length; i++) {
				Matcher matcher = patterns[i].matcher(value);
				if (matcher.matches()) {
					int count = matcher.groupCount();
					if (count == 1) {
						return matcher.group(1);
					}
					StringBuffer buffer = new StringBuffer();
					for (int j = 0; j < count; j++) {
						String component = matcher.group(j + 1);
						if (component != null) {
							buffer.append(component);
						}
					}
					return buffer.toString();
				}
			}
			return null;
		}

		/**
		 * Provide a String representation of this validator.
		 * 
		 * @return A String representation of this validator
		 */
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("RegexValidator{");
			for (int i = 0; i < patterns.length; i++) {
				if (i > 0) {
					buffer.append(",");
				}
				buffer.append(patterns[i].pattern());
			}
			buffer.append("}");
			return buffer.toString();
		}

	}
}

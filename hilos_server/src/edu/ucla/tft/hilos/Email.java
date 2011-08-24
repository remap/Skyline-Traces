package edu.ucla.tft.hilos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * A class representing an email. A set of email templates for different
 * languages is provided which can be personalized before being written out.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Email {
	public static final VerifyEmail verifyEmail = new VerifyEmail();
	public static final ResetPassword resetPassword = new ResetPassword();

	public final String to;
	public final String subject;
	public final String text;

	public Email(String to, String subject, String text) {
		this.to = to;
		this.subject = subject;
		this.text = text;
	}

	private static abstract class EmailTemplate {
		private static class Content {
			public final String subjectTemplate;
			public final String textTemplate;

			public Content(String subjectTemplate, String textTemplate) {
				this.subjectTemplate = subjectTemplate;
				this.textTemplate = textTemplate;
			}
		}

		private Map<String, Content> contentByLanguage = new HashMap<String, Content>();

		protected Email create(ServletContext ctx, String to,
				Map<String, String> map, String lang) throws Exception {
			Content content;
			if ((content = contentByLanguage.get(lang)) == null) {
				try {
					BufferedReader r = new BufferedReader(new FileReader(
							ctx.getRealPath(getTemplateFilename(lang))));
					String subjectTemplate = r.readLine();
					r.readLine(); // separator
					String line;
					StringBuilder sb = new StringBuilder();
					while ((line = r.readLine()) != null) {
						if (sb.length() > 0) {
							sb.append("\n");
						}
						sb.append(line);
					}
					String textTemplate = sb.toString();
					content = new Content(subjectTemplate, textTemplate);
					contentByLanguage.put(lang, content);
					r.close();
				} catch (IOException e) {
					throw new Exception("Could not read template: "
							+ getClass());
				}
			}
			String subject, text;
			subject = content.subjectTemplate;
			text = content.textTemplate;
			for (String key : map.keySet()) {
				if (subject.contains(key)) {
					subject = subject.replace(key, map.get(key));
				}
				if (text.contains(key)) {
					text = text.replace(key, map.get(key));
				}
			}
			return new Email(to, subject, text);
		}

		protected abstract String getTemplateFilename(String lang);
	}

	public static class VerifyEmail extends EmailTemplate {
		private VerifyEmail() {
		}

		public Email create(ServletContext ctx, String to, String name,
				String url, String lang) throws Exception {
			Map<String, String> map = new HashMap<String, String>();
			map.put("$EMAIL_", to);
			map.put("$NAME_", name);
			map.put("$URL_", url);
			return create(ctx, to, map, lang);
		}

		@Override
		protected String getTemplateFilename(String lang) {
			return "email_templates/" + lang + "/verifyEmail.txt";
		}
	}

	public static class ResetPassword extends EmailTemplate {
		private ResetPassword() {
		}

		public Email create(ServletContext ctx, String to, String name,
				String password, String lang) throws Exception {
			Map<String, String> map = new HashMap<String, String>();
			map.put("$EMAIL_", to);
			map.put("$NAME_", name);
			map.put("$PASSWORD_", password);
			return create(ctx, to, map, lang);
		}

		@Override
		protected String getTemplateFilename(String lang) {
			return "email_templates/" + lang + "/resetPassword.txt";
		}
	}
}
package edu.ucla.tft.hilos;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A queue for emails to be sent out.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class EmailQueue {
	private static EmailQueue instance;

	public static EmailQueue get() {
		if (instance == null) {
			instance = new EmailQueue();
		}
		return instance;
	}

	private Queue<Email> queue;
	private Thread processorThread;
	private Session session;

	private EmailQueue() {
		queue = new LinkedList<Email>();
		Properties props = new Properties();
		props.put("mail.smtp.user", "hilos.remap.ucla@gmail.com");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", 465);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.debug", "true");
		props.put("mail.smtp.socketFactory.port", 465);
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		try {
			Authenticator auth = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
							"hilos.remap.ucla@gmail.com", "hilos@imlab");
				}
			};
			session = Session.getInstance(props, auth);
		} catch (Exception mex) {
			mex.printStackTrace();
		}
		processorThread = new Thread() {
			public void run() {
				while (true) {
					Email item = null;
					try {
						synchronized (EmailQueue.this) {
							while ((item = getItem()) == null) {
								EmailQueue.this.wait();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (item != null) {
						process(item);
					}
				}
			}
		};
		processorThread.start();
	}

	private boolean process(Email item) {
		try {
			MimeMessage msg = new MimeMessage(session);
			msg.setText(item.text);
			msg.setSubject(item.subject);
			msg.setFrom(new InternetAddress("hilos.remap.ucla@gmail.com",
					"Hilos"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
					item.to));
			Transport.send(msg);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private synchronized Email getItem() {
		return queue.poll();
	}

	public synchronized void addItem(Email item) {
		queue.add(item);
		notifyAll();
	}
}

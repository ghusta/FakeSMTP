package com.nilhcem.fakesmtp.server;

import com.nilhcem.fakesmtp.core.ArgsHandler;
import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.model.UIModel;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Saves emails and notifies components, so they can refresh their views with new data.
 *
 * @author Nilhcem
 * @since 1.0
 */
@NullMarked
public final class MailSaver {

	private static final Logger log = LoggerFactory.getLogger(MailSaver.class);

	private static final String LINE_SEPARATOR = System.lineSeparator();
	// This can be a static variable since it is Thread Safe
	private static final Pattern SUBJECT_PATTERN = Pattern.compile("^Subject: (.*)$");

	/*
		DateTimeFormatter symbols used :
		S fraction-of-second fraction 978
		n nano-of-second number 987654321
		H hour-of-day (0-23) number 0
	 */

	/**
	 * Use HH for (0-23) hour format.
	 * Use n to use nanos (9 digits) instead of milliseconds (3 digits)
	 */
	private final DateTimeFormatter dateTimeFormatForFilename = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.nnnnnnnnn");

	private final SubmissionPublisher<EmailModel> emailPublisher;

	public MailSaver() {
		// can pass Executor as arg
		this.emailPublisher = new SubmissionPublisher<>();
	}

	public Flow.Publisher<EmailModel> getEmailPublisher() {
		return emailPublisher;
	}

	/**
	 * Saves incoming email in file system and notifies observers.
	 *
	 * @param from the user who send the email.
	 * @param recipients the recipients of the email.
	 * @param data an InputStream object containing the email.
	 * @see com.nilhcem.fakesmtp.gui.MainPanel#addObservers to see which observers will be notified
	 */
	public void saveEmailAndNotify(String from, List<String> recipients, InputStream data) {
		List<String> relayDomains = UIModel.INSTANCE.getRelayDomains();

		if (relayDomains != null) {
			for (String recipient : recipients) {
				boolean matches = relayDomains.stream()
						.anyMatch(recipient::endsWith);
				if (!matches) {
					log.debug("Recipient '{}' doesn't match relay domains", recipient);
					return;
				}
			}
		}

		// We move everything that we can move outside the synchronized block to limit the impact
		String mailContent = convertStreamToString(data);
		String subject = getSubjectFromStr(mailContent);

		synchronized (getLock()) {
			String filePath = saveEmailToFile(mailContent);
			EmailModel model = new EmailModel(LocalDateTime.now(),
					from, recipients,
					subject, mailContent,
					(filePath != null ? Path.of(filePath) : null));

			emailPublisher.submit(model);
		}
	}

	public void saveEmailAndNotify(String from, List<String> recipients, String messageContent) {
		List<String> relayDomains = UIModel.INSTANCE.getRelayDomains();

		if (relayDomains != null) {
			for (String recipient : recipients) {
				boolean matches = relayDomains.stream()
						.anyMatch(recipient::endsWith);
				if (!matches) {
					log.debug("Recipient '{}' doesn't match relay domains", recipient);
					return;
				}
			}
		}

		// We move everything that we can move outside the synchronized block to limit the impact
		String mailContent = messageContent;
		String subject = getSubjectFromStr(mailContent);

		synchronized (getLock()) {
			String filePath = saveEmailToFile(mailContent);
			EmailModel model = new EmailModel(LocalDateTime.now(),
					from, recipients,
					subject, mailContent,
					(filePath != null ? Path.of(filePath) : null));

			emailPublisher.submit(model);
		}
	}

	/**
	 * Deletes all received emails from file system.
	 */
	public void deleteEmails() {
		Map<Integer, String> mails = UIModel.INSTANCE.getListMailsMap();
		if (ArgsHandler.INSTANCE.isMemoryModeEnabled()) {
			return;
		}
		for (String value : mails.values()) {
			try {
				if (!Files.deleteIfExists(Paths.get(value))) {
					log.error("Impossible to delete file {}", value);
				}
			} catch (IOException | SecurityException e) {
				log.error(e.toString(), e);
			}
		}
	}

	/**
	 * Returns a lock object.
	 * <p>
	 * This lock will be used to make the application thread-safe, and
	 * avoid receiving and deleting emails in the same time.
	 * </p>
	 *
	 * @return a lock object <i>(which is actually the current instance of the {@code MailSaver} object)</i>.
	 */
	public Object getLock() {
		return this;
	}

	/**
	 * Converts an {@code InputStream} into a {@code String} object.
	 * <p>
	 * The method will copy the Received headers lines of the input stream depending
	 * of SubEtha SMTP builder configuration (insertReceivedHeaders).
	 * </p>
	 *
	 * @param is the InputStream to be converted.
	 * @return the converted string object, containing data from the InputStream passed in parameters.
	 */
	private String convertStreamToString(InputStream is) {
		// see: org.subethamail.smtp.internal.io.ReceivedHeaderStream
		// see: org.subethamail.smtp.server.SMTPServer.getDisableReceivedHeaders
		// and: org.subethamail.smtp.server.SMTPServer.Builder.insertReceivedHeaders(boolean)
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append(LINE_SEPARATOR);
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return sb.toString();
	}

	/**
	 * Saves the content of the email passed in parameters in a file.
	 *
	 * @param mailContent the content of the email to be saved.
	 * @return the path of the created file.
	 */
	private @Nullable String saveEmailToFile(String mailContent) {
		if (ArgsHandler.INSTANCE.isMemoryModeEnabled()) {
			return null;
		}
		Path saveDirectory = Path.of(UIModel.INSTANCE.getSavePath());
		if (!Files.exists(saveDirectory) || !Files.isDirectory(saveDirectory)) {
			try {
				Files.createDirectory(saveDirectory);
			} catch (IOException e) {
				log.error(e.toString());
			}
		}
		String filePath = "%s%s%s".formatted(saveDirectory, File.separator,
				dateTimeFormatForFilename.format(LocalDateTime.now()));

		// Create file
		int i = 0;
		File file = null;
		while (file == null || file.exists()) {
			String iStr;
			if (i++ > 0) {
				iStr = "_" + i;
			} else {
				iStr = "";
			}
			file = new File(filePath + iStr + Configuration.getInstance().get("emails.suffix"));
		}

		// Copy String to file
		try {
			Files.writeString(file.toPath(), mailContent, Charset.defaultCharset(), CREATE_NEW, WRITE);
		} catch (IOException e) {
			// If we can't save file, we display the error in the SMTP logs
			Logger smtpLogger = LoggerFactory.getLogger(org.subethamail.smtp.server.Session.class);
			smtpLogger.error("Error: Can't save email: {}", e.toString());
			return null;
		}
		return file.getAbsolutePath();
	}

	/**
	 * Gets the subject from the email data passed in parameters.
	 *
	 * @param data a string representing the email content.
	 * @return the subject of the email, or an empty subject if not found.
	 */
	private String getSubjectFromStr(String data) {
		try {
			BufferedReader reader = new BufferedReader(new StringReader(data));

			String line;
			while ((line = reader.readLine()) != null) {
				 Matcher matcher = SUBJECT_PATTERN.matcher(line);
				 if (matcher.matches()) {
					 return matcher.group(1);
				 }
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return "";
	}
}

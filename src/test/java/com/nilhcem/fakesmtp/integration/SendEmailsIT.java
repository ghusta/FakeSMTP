package com.nilhcem.fakesmtp.integration;

import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.test.TestConfig;
import org.apache.commons.mail2.core.EmailConstants;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.Email;
import org.apache.commons.mail2.jakarta.EmailAttachment;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.apache.commons.mail2.jakarta.MultiPartEmail;
import org.apache.commons.mail2.jakarta.SimpleEmail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public final class SendEmailsIT {

	private static final Logger logger = LoggerFactory.getLogger(SendEmailsIT.class);

	private static final String EMOJI_EMAIL = "📧";
	private static final String EMOJI_LOVE_LETTER = "💌";
	private static final String EMOJI_HAPPY_FACE = "😀";
	private static final String EMOJI_ALIEN = "👽";

	@BeforeAll
	public static void displayInfo() {
		logger.info("Launching integration tests...");
		logger.info("You need to run the project and launch the SMTP server on port {} before testing.", TestConfig.PORT_INTEGRATION_TESTS);
	}

	@Test
	void sendSimpleTestEmail() throws EmailException {
		Email email = new SimpleEmail();
		email.setHostName(TestConfig.HOST);
		email.setSmtpPort(TestConfig.PORT_INTEGRATION_TESTS);
		email.setStartTLSEnabled(true);
		email.setFrom("user@gmail.com");
		email.setSubject("Simple email");
		email.setMsg("This is a simple plain text email :-)");
		email.addTo("foo@bar.com");
		email.send();
	}

	@RepeatedTest(3)
	void sendSimpleTestEmailWithUTF8() throws EmailException {
		Email email = new SimpleEmail();
		email.setHostName(TestConfig.HOST);
		email.setSmtpPort(TestConfig.PORT_INTEGRATION_TESTS);
		email.setStartTLSEnabled(true);
		email.setFrom("user@gmail.com");
		email.addTo("foo@bar.com");
		email.addHeader("Content-Type", EmailConstants.TEXT_PLAIN + "; charset=" + StandardCharsets.UTF_8);
		email.setSubject(EMOJI_EMAIL + " Simple email using Unicode / Emojis " + EMOJI_LOVE_LETTER);
		email.setMsg("This is a simple plain text email / UTF-8 \n" +
				"\\_" + EMOJI_HAPPY_FACE + "_/ \n"
				+ "\n"
				+ EMOJI_ALIEN);
		email.send();
	}

	@Test
	void sendEmailWithAttachment() throws EmailException {
		// Create the attachment
		EmailAttachment attachment = new EmailAttachment();
		attachment.setPath("src/main/resources" + Configuration.getInstance().get("application.icon.path"));
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("Image file");
		attachment.setName("icon.gif");

		// Create the email message
		MultiPartEmail email = new MultiPartEmail();
		email.setHostName(TestConfig.HOST);
		email.setSmtpPort(TestConfig.PORT_INTEGRATION_TESTS);
		email.addTo("jdoe@somewhere.org", "John Doe");
		email.setFrom("me@example.org", "Me");
		email.setSubject("File attachment");
		email.setMsg("This email contains an enclosed file.");

		// Add the attachment
		email.attach(attachment);

		// Send the email
		email.send();
	}

	@Test
	void sendHTMLFormattedEmail() throws EmailException {
		// Create the email message
		HtmlEmail email = new HtmlEmail();
		email.setHostName(TestConfig.HOST);
		email.setSmtpPort(TestConfig.PORT_INTEGRATION_TESTS);
		email.addTo("jdoe@somewhere.org", "John Doe");
		email.setFrom("me@example.org", "Me");
		email.setSubject("HTML email");

		// Set the HTML message
		email.setHtmlMsg("<html><body>This is an <b>HTML</b> email.<br /><br /></body></html>");

		// Set the alternative message
		email.setTextMsg("Your email client does not support HTML messages");

		// Send the email
		email.send();
	}

	@Test
	void sendEmailWithBase64Subject() throws EmailException {
		Email email = new SimpleEmail();
		email.setHostName(TestConfig.HOST);
		email.setSmtpPort(TestConfig.PORT_INTEGRATION_TESTS);
		email.setFrom("spammy@example.org");
		email.addTo("foo@bar.com");
		email.setSubject("=?UTF-8?B?4pyIIEJvc3RvbiBhaXJmYXJlIGRlYWxzIC0gd2hpbGUgdGhleSBsYXN0IQ==?=");
		email.setMsg("Not really interesting, huh?");
		email.send();
	}

	@Test
	void sendEmailToManyRecipientsWithTwoHeaders() throws EmailException {
		Email email = new SimpleEmail();
		email.setHostName(TestConfig.HOST);
		email.setSmtpPort(TestConfig.PORT_INTEGRATION_TESTS);
		email.setFrom("info@example.com");
		email.addTo("test1@example.com");
		email.addTo("test2@example.com");
		email.addHeader("Foo", "Bar");
		email.addHeader("Foo2", "Bar2");
		email.setSubject("Hi");
		email.setMsg("Just to check if everything is OK");
		email.send();
	}

	@Test
	void sendEmailToMultipleRecipients() throws EmailException {
		Email email = new SimpleEmail();
		email.setHostName(TestConfig.HOST);
		email.setSmtpPort(TestConfig.PORT_INTEGRATION_TESTS);
		email.setFrom("info@example.com");
		email.addTo("test1@example.com");
		email.addTo("test2@example.com");
		email.addCc("test3@example.com");
		email.addCc("test4@example.com");
		email.addBcc("test5@example.com");
		email.addBcc("test6@example.com");
		email.setSubject("Hi everyone");
		email.setMsg("Hello to everyone !");
		email.send();
	}

	@Test
	void sendEmailWithDots() throws EmailException {
		Email email = new SimpleEmail();
		email.setDebug(true);
		email.setHostName(TestConfig.HOST);
		email.setSmtpPort(TestConfig.PORT_INTEGRATION_TESTS);
		email.setFrom("user@example.com");
		email.addTo("foo@example.com");
		email.setSubject("Two dots separated with a new line");
		email.setMsg(".\n.");
		email.send();
	}
}

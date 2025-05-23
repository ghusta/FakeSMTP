package com.nilhcem.fakesmtp.core.server;

import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.model.UIModel;
import com.nilhcem.fakesmtp.server.MailSaver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Observer;

import static org.assertj.core.api.Assertions.assertThat;

public class MailServerTest {
	private static MailSaver saver;

	@BeforeAll
	public static void createMailSaver() {
		saver = new MailSaver();
	}

	@Test
	void testGetLock() {
		assertThat(saver.getLock()).isSameAs(saver);
	}

	@Test
	@Disabled
	void testSaveDeleteEmail() {
		final String from = "from@example.com";
		final String to = "to@example.com";
		final String subject = "Hello";
		final String content = "How are you?";

		// Save
		final InputStream data = fromString(getMockEmail(from, to, subject, content));
		Observer mockObserver = (o, arg) -> {
			EmailModel model = (EmailModel)arg;

			assertThat(model.from()).isEqualTo(from);
			assertThat(model.recipients().get(0)).isEqualTo(to);
			assertThat(model.subject()).isEqualTo(subject);
			assertThat(model.emailContent()).isNotEmpty();
			assertThat(model.filePath())
					.isNotNull()
					.isNotEmptyFile();

			// Delete
			UIModel.INSTANCE.getListMailsMap().put(0, String.valueOf(model.filePath()));
			saver.deleteEmails();
			assertThat(model.filePath()).doesNotExist();
		};
//		saver.addObserver(mockObserver);
//		assertThat(saver.countObservers()).isNotZero();
//		saver.saveEmailAndNotify(from, to, data);
//		saver.deleteObserver(mockObserver);
	}

	private String getMockEmail(String from, String to, String subject, String content) {
		String br = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder()
			.append("Line 1 will be removed").append(br)
			.append("Line 2 will be removed").append(br)
			.append("Line 3 will be removed").append(br)
			.append("Line 4 will be removed").append(br)
			.append("Date: Thu, 15 May 2042 04:42:42 +0800 (CST)").append(br)
			.append("From: \"%s\" <%s>%n".formatted(from, from))
			.append("To: \"%s\" <%s>%n".formatted(to, to))
			.append("Message-ID: <17000042.0.1300000000042.JavaMail.wtf@OMG00042>").append(br)
			.append("Subject: %s%n".formatted(subject))
			.append("MIME-Version: 1.0").append(br)
			.append("Content-Type: text/plain; charset=us-ascii").append(br)
			.append("Content-Transfer-Encoding: 7bit").append(br).append(br)
			.append(content).append(br).append(br);
		return sb.toString();
	}

	private InputStream fromString(String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		return new ByteArrayInputStream(bytes);
	}
}

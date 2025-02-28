package com.nilhcem.fakesmtp.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * A model representing a received email.
 * <p>
 * This object will be created and sent to observers by the {@code MailSaver} object.<br>
 * It contains useful data such as the content of the email and its path in the file system.
 * </p>
 *
 * @author Nilhcem
 * @since 1.0
 */
public record EmailModel(
		LocalDateTime receivedDate,
		String from,
		String recipient,
		String subject,
		String emailContent,
		Path filePath
) {
}
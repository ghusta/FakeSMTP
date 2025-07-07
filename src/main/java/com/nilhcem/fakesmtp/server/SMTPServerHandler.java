package com.nilhcem.fakesmtp.server;

import com.nilhcem.fakesmtp.core.exception.BindPortException;
import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Starts and stops the SMTP server.
 *
 * @author Nilhcem
 * @since 1.0
 */
@NullMarked
public enum SMTPServerHandler {
	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger(SMTPServerHandler.class);

	@Getter
	private final MailSaver mailSaver = new MailSaver();
	private final MailListener myListener = new MailListener(mailSaver);
	@Getter
	private SMTPServer smtpServer;

	SMTPServerHandler() {
	}

	/**
	 * Starts the server on the port and address specified in parameters.
	 *
	 * @param port the SMTP port to be opened.
	 * @param bindAddress the address to bind to. null means bind to all.
	 * @throws BindPortException when the port can't be opened.
	 * @throws OutOfRangePortException when port is out of range.
	 * @throws IllegalArgumentException when port is out of range.
	 */
	public void startServer(int port, @Nullable InetAddress bindAddress) throws BindPortException, OutOfRangePortException, UnknownHostException {
		log.debug("Starting server on port {}", port);
		try {
			InetAddress anyLocalAddress = InetAddress.getByName("0.0.0.0");
			MessageHandlerFactory mhf = ctx -> new MultipleRecipientsMessageHandler(mailSaver);
			smtpServer = new SMTPServer.Builder()
					.insertReceivedHeaders(false)
//					.simpleMessageListener(myListener)
					.messageHandlerFactory(mhf)
					.authenticationHandlerFactory(new SMTPAuthHandlerFactory())
					.bindAddress(bindAddress == null ? anyLocalAddress : bindAddress)
					.port(port)
					.build();
			smtpServer.start();
		} catch (RuntimeException exception) {
			if (exception.getMessage().contains("BindException")) { // Can't open port
				log.error("{}. Port {}", exception.getMessage(), port);
				throw new BindPortException(exception, port);
			} else if (exception.getMessage().contains("out of range")) { // Port out of range
				log.error("Port {} out of range.", port);
				throw new OutOfRangePortException(exception, port);
			} else { // Unknown error
				log.error("", exception);
				throw exception;
			}
		}
	}

	/**
	 * Stops the server.
	 * <p>
	 * If the server is not started, does nothing special.
	 * </p>
	 */
	public void stopServer() {
		if (smtpServer != null && smtpServer.isRunning()) {
			log.debug("Stopping server");
			smtpServer.stop();
		}
	}

}

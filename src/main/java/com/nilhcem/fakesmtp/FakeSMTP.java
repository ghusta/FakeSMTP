package com.nilhcem.fakesmtp;

import com.apple.eawt.Application;
import com.nilhcem.fakesmtp.core.ArgsHandler;
import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.exception.UncaughtExceptionHandler;
import com.nilhcem.fakesmtp.gui.MainFrame;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Entry point of the application.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class FakeSMTP {

	private static final Logger log = LoggerFactory.getLogger(FakeSMTP.class);

	private FakeSMTP() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks command line arguments, sets some specific properties, and runs the main window.
	 * <p>
	 * Before opening the main window, this method will:
     * </p>
	 * <ul>
	 *   <li>check command line arguments, and possibly display an error dialog,</li>
	 *   <li>set a default uncaught exception handler to intercept every uncaught exception;</li>
	 *   <li>use a custom icon in the Mac Dock;</li>
	 *   <li>set a property for Mac OS X to take the menu bar off the JFrame;</li>
	 *   <li>set a property for Mac OS X to set the name of the application menu item;</li>
	 *   <li>turn off the bold font in all components for swing default theme;</li>
	 *   <li>use the platform look and feel.</li>
	 * </ul>
	 *
	 * @param args a list of command line parameters.
	 */
	public static void main(final String[] args) {
		try {
			ArgsHandler.INSTANCE.handleArgs(args);
		} catch (ParseException e) {
			log.error(e.toString());
			ArgsHandler.INSTANCE.displayUsage();
			return;
		}

		if (ArgsHandler.INSTANCE.isPrintHelp()) {
			ArgsHandler.INSTANCE.displayUsage();
			return;
		}

		if (ArgsHandler.INSTANCE.shouldStartInBackground()) {
			// option AutoStart is useless (implied)
			try {
				log.info("Running on Java Runtime {}", Runtime.version());
				log.info("Starting server...");
				SMTPServerHandler.INSTANCE.startServer(getPort(), getBindAddress());
			} catch (NumberFormatException e) {
				log.error("Error: Invalid port number", e);
			} catch (UnknownHostException e) {
				log.error("Error: Invalid bind address", e);
			} catch (Exception e) {
				log.error("Failed to auto-start server in background", e);
			}
		} else {
            System.setProperty("mail.mime.decodetext.strict", "false");
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

            EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						URL envelopeImage = getClass().getResource(Configuration.getInstance().get("application.icon.path"));
						if (envelopeImage != null) {
							Application.getApplication().setDockIconImage(Toolkit.getDefaultToolkit().getImage(envelopeImage));
						}
					} catch (RuntimeException e) {
						log.debug("Error: {} - This is probably because we run on a non-Mac platform and these components are not implemented", e.getMessage());
					} catch (Exception e) {
						log.error("", e);
					}

					System.setProperty("apple.laf.useScreenMenuBar", "true");
					System.setProperty("com.apple.mrj.application.apple.menu.about.name", Configuration.getInstance().get("application.name"));
					UIManager.put("swing.boldMetal", Boolean.FALSE);
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception e) {
						log.error("", e);
					}
					// Start GUI
					new MainFrame();
				}
			});
		}
	}

	/**
	 * @return either the default port, or the custom port, if specified.
	 * @throws NumberFormatException if the specified port cannot be parsed to an integer.
	 */
	private static int getPort() throws NumberFormatException {
		Optional<Integer> port = ArgsHandler.INSTANCE.getPort();
		return port.orElseGet(() -> Integer.parseInt(Configuration.getInstance().get("smtp.default.port")));
	}

	/**
	 * @return an InetAddress representing the specified bind address, or null, if not specified
	 * @throws UnknownHostException if the bind address is invalid
	 */
	private static InetAddress getBindAddress() throws UnknownHostException {
		String bindAddressStr = ArgsHandler.INSTANCE.getBindAddress();
		if (bindAddressStr == null || bindAddressStr.isEmpty()) {
			return null;
		}
		return InetAddress.getByName(bindAddressStr);
	}
}

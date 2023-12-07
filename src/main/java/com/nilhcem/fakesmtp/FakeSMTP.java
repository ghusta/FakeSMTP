package com.nilhcem.fakesmtp;

import java.awt.EventQueue;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import javax.swing.UIManager;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.ParseException;

import com.nilhcem.fakesmtp.core.ArgsHandler;
import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.exception.UncaughtExceptionHandler;
import com.nilhcem.fakesmtp.gui.MainFrame;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;

/**
 * Entry point of the application.
 *
 * @author Nilhcem
 * @since 1.0
 */
@Slf4j
public final class FakeSMTP {

	public static final String OS_NAME = System.getProperty("os.name", "");
	/**
	 * See : org.apache.commons.lang3.SystemUtils#IS_OS_MAC
	 */
	public static final boolean IS_OS_MAC = OS_NAME.startsWith("Mac");

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

			EventQueue.invokeLater(() -> {
				if (IS_OS_MAC) {
					// see (written in 2003) : https://www.oracle.com/technical-resources/articles/javase/javatomac.html
					// see also : https://bugs.openjdk.org/browse/JDK-8188085
					System.setProperty("apple.laf.useScreenMenuBar", "true");
					System.setProperty("com.apple.mrj.application.apple.menu.about.name", Configuration.getInstance().get("application.name"));
				}
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				try {
					// see : https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					log.error("", e);
				}
                // Start GUI
				new MainFrame();
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

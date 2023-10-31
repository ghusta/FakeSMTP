
package com.nilhcem.fakesmtp.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.nilhcem.fakesmtp.model.UIModel;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles command line arguments.
 *
 * @author Nilhcem
 * @since 1.3
 */
public enum ArgsHandler {
	INSTANCE;

	private static final String OPT_EMAILS_DIR_SHORT = "o";
	private static final String OPT_EMAILS_DIR_LONG = "output-dir";
	private static final String OPT_EMAILS_DESC = "Emails output directory";
	private final Option optionEmailsDir = Option.builder(OPT_EMAILS_DIR_SHORT)
			.longOpt(OPT_EMAILS_DIR_LONG)
			.hasArg(true)
			.required(false)
			.desc(OPT_EMAILS_DESC)
			.build();

	private static final String OPT_AUTOSTART_SHORT = "s";
	private static final String OPT_AUTOSTART_LONG = "start-server";
	private static final String OPT_AUTOSTART_DESC = "Automatically starts the SMTP server at launch";
	private final Option optionAutoStart = Option.builder(OPT_AUTOSTART_SHORT)
			.longOpt(OPT_AUTOSTART_LONG)
			.hasArg(false)
			.desc(OPT_AUTOSTART_DESC)
			.build();

	private static final String OPT_PORT_SHORT = "p";
	private static final String OPT_PORT_LONG = "port";
	private static final String OPT_PORT_DESC = "SMTP port number";
	private final Option optionPort = Option.builder(OPT_PORT_SHORT)
			.longOpt(OPT_PORT_LONG)
			.hasArg(true)
			.type(Number.class)
			.desc(OPT_PORT_DESC)
			.build();

	private static final String OPT_BACKGROUNDSTART_SHORT = "b";
	private static final String OPT_BACKGROUNDSTART_LONG = "background";
	private static final String OPT_BACKGROUNDSTART_DESC = "If specified, does not start the GUI. Must be used with the -" + OPT_AUTOSTART_SHORT + " (--" +  OPT_AUTOSTART_LONG + ") argument";
	private final Option optionBackgroundStart = Option.builder(OPT_BACKGROUNDSTART_SHORT)
			.longOpt(OPT_BACKGROUNDSTART_LONG)
			.hasArg(false)
			.desc(OPT_BACKGROUNDSTART_DESC)
			.build();

	private static final String OPT_RELAYDOMAINS_SHORT = "r";
	private static final String OPT_RELAYDOMAINS_LONG = "relay-domains";
	private static final String OPT_RELAYDOMAINS_DESC = "Comma separated email domain(s) for which relay is accepted. If not specified, relays to any domain. If specified, relays only emails matching these domain(s), dropping (not saving) others";
	private static final char OPT_RELAYDOMAINS_SEPARATOR = ',';
	private final Option optionRelayDomains = Option.builder(OPT_RELAYDOMAINS_SHORT)
			.longOpt(OPT_RELAYDOMAINS_LONG)
			.hasArgs()
			.valueSeparator(OPT_RELAYDOMAINS_SEPARATOR)
			.required(false)
			.desc(OPT_RELAYDOMAINS_DESC)
			.build();

	private static final String OPT_MEMORYMODE_SHORT = "m";
	private static final String OPT_MEMORYMODE_LONG = "memory-mode";
	private static final String OPT_MEMORYMODE_DESC = "Disable the persistence in order to avoid the overhead that it adds";
	private final Option optionMemoryMode = Option.builder(OPT_MEMORYMODE_SHORT)
			.longOpt(OPT_MEMORYMODE_LONG)
			.hasArg(false)
			.desc(OPT_MEMORYMODE_DESC)
			.build();

	private static final String OPT_BINDADDRESS_SHORT = "a";
	private static final String OPT_BINDADDRESS_LONG = "bind-address";
	private static final String OPT_BINDADDRESS_DESC = "IP address or hostname to bind to. Binds to all local IP addresses if not specified. Only works together with the -" + OPT_BACKGROUNDSTART_SHORT + " (--" +  OPT_BACKGROUNDSTART_LONG + ") argument.";
	private final Option optionBindAddress = Option.builder(OPT_BINDADDRESS_SHORT)
			.longOpt(OPT_BINDADDRESS_LONG)
			.hasArg(true)
			.required(false)
			.desc(OPT_BINDADDRESS_DESC)
			.build();

	private static final String OPT_EMLVIEWER_SHORT = "e";
	private static final String OPT_EMLVIEWER_LONG = "eml-viewer";
	private static final String OPT_EMLVIEWER_DESC = "Executable of program used for viewing emails";
	private final Option optionEmlViewer = Option.builder(OPT_EMLVIEWER_SHORT)
			.longOpt(OPT_EMLVIEWER_LONG)
			.hasArg(true)
			.desc(OPT_EMLVIEWER_DESC)
			.build();

	private final Options options;

	private String port;
	private String bindAddress;
	private String outputDirectory;
	private String emlViewer;
	private boolean backgroundStart;
	private boolean startServerAtLaunch;
	private boolean memoryModeEnabled;
	private boolean printHelp = false;

	/**
	 * Handles command line arguments.
	 */
	ArgsHandler() {
		options = new Options()
				.addOption(optionEmailsDir)
				.addOption(optionAutoStart)
				.addOption(optionPort)
				.addOption(optionBindAddress)
				.addOption(optionBackgroundStart)
				.addOption(optionRelayDomains)
				.addOption(optionMemoryMode)
				.addOption(optionEmlViewer)
				.addOption(Option.builder("h").longOpt("help").desc("Print this message").build());
	}

	/**
	 * Interprets command line arguments.
	 *
	 * @param args program's arguments.
	 * @throws ParseException when arguments are invalid.
	 */
	public void handleArgs(String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		outputDirectory = cmd.getOptionValue(optionEmailsDir);
		if (outputDirectory != null) {
			UIModel.INSTANCE.setSavePath(outputDirectory);
		}

		port = cmd.getOptionValue(optionPort);
		bindAddress = cmd.getOptionValue(optionBindAddress);
		startServerAtLaunch = cmd.hasOption(optionAutoStart);
		backgroundStart = cmd.hasOption(optionBackgroundStart);
		memoryModeEnabled = cmd.hasOption(optionMemoryMode);
		emlViewer = cmd.getOptionValue(optionEmlViewer);
		printHelp = cmd.hasOption("h");

		// Change SMTP server log level to info if memory mode was enabled to improve performance
		if (memoryModeEnabled) {
			((Logger) LoggerFactory.getLogger(org.subethamail.smtp.server.Session.class)).setLevel(Level.INFO);
		}

		String[] relaydomains = cmd.getOptionValues(optionRelayDomains);
		if (relaydomains != null) {
			List<String> values = Stream.of(relaydomains)
					.map(String::trim)
					.filter(s -> !s.isBlank())
					.collect(Collectors.toList());
			UIModel.INSTANCE.setRelayDomains(values);
		}

		// Host binding for GUI
		if (bindAddress != null) {
			UIModel.INSTANCE.setHost(bindAddress);
		}
	}

	/**
	 * Displays the app's usage in the standard output.
	 */
	public void displayUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(String.format(Locale.US, "java -jar %s [OPTION]...", getJarName()), options);
	}

	public boolean printHelp() {
		return printHelp;
	}

	/**
	 * @return whether the SMTP server must be started automatically at launch.
	 */
	public boolean shouldStartServerAtLaunch() {
		return startServerAtLaunch;
	}

	/**
	 * @return whether the SMTP server must be running without a GUI, only if started at launch (if {@code shouldStartServerAtLaunch()} returns true).
	 * @see #shouldStartServerAtLaunch
	 */
	public boolean shouldStartInBackground() {
		return startServerAtLaunch && backgroundStart;
	}

	/**
	 * @return the port, as specified by the user, or a {@code null} string if unspecified.
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @return the bind address, as specified by the user, or a {@code null} string if unspecified.
	 */
	public String getBindAddress() {
		return bindAddress;
	}

	/**
	 * @return the output directory, as specified by the user, or a {@code null} string if unspecified.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * @return whether the SMTP server should disable the persistence in order to avoid the overhead that it adds.
	 * This is particularly useful when we launch performance tests that massively send emails.
	 */
	public boolean memoryModeEnabled() {
		return memoryModeEnabled;
	}

	/**
	 * @return the name of executable used for viewing eml files, as specified by the user, or a {@code null} string if unspecified.
	 */
	public String getEmlViewer() {
		return emlViewer;
	}

	/**
	 * @return the file name of the program.
	 */
	private String getJarName() {
		return new java.io.File(
				ArgsHandler.class.getProtectionDomain()
				.getCodeSource()
				.getLocation()
				.getPath())
		.getName();
	}
}

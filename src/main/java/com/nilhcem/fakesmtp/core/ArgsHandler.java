
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

	private final Option optionEmailsDir = Option.builder("o")
			.longOpt("output-dir")
			.hasArg(true)
			.required(false)
			.desc("Emails output directory")
			.build();

	private final Option optionAutoStart = Option.builder("s")
			.longOpt("start-server")
			.hasArg(false)
			.desc("Automatically starts the SMTP server at launch")
			.build();

	private final Option optionPort = Option.builder("p")
			.longOpt("port")
			.hasArg(true)
			.type(Number.class)
			.desc("SMTP port number")
			.build();

	private final Option optionBackgroundStart = Option.builder("b")
			.longOpt("background")
			.hasArg(false)
			.desc("If specified, does not start the GUI. Must be used with the -s (--start-server) argument")
			.build();

	private final Option optionRelayDomains = Option.builder("r")
			.longOpt("relay-domains")
			.hasArgs()
			.valueSeparator(',')
			.required(false)
			.desc("Comma separated email domain(s) for which relay is accepted. If not specified, relays to any domain. " +
					"If specified, relays only emails matching these domain(s), dropping (not saving) others")
			.build();

	private final Option optionMemoryMode = Option.builder("m")
			.longOpt("memory-mode")
			.hasArg(false)
			.desc("Disable the persistence in order to avoid the overhead that it adds")
			.build();

	private final Option optionBindAddress = Option.builder("a")
			.longOpt("bind-address")
			.hasArg(true)
			.required(false)
			.desc("IP address or hostname to bind to. Binds to all local IP addresses if not specified. " +
					"Only works together with the -b (--background) argument.")
			.build();

	private final Option optionEmlViewer = Option.builder("e")
			.longOpt("eml-viewer")
			.hasArg(true)
			.desc("Executable of program used for viewing emails")
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
					.toList();
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

package com.nilhcem.fakesmtp.core;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.nilhcem.fakesmtp.core.Configuration.Settings.EMAILS_DEFAULT_DIR;
import static com.nilhcem.fakesmtp.core.Configuration.Settings.SMTP_DEFAULT_PORT;

/**
 * Contains and returns some project-specific configuration variables.
 *
 * @author Nilhcem
 * @since 1.0
 */
@Slf4j
public class Configuration {

	private static final Configuration INSTANCE = new Configuration();

	private static final String CONFIG_FILE = "/configuration.properties";
	private static final String USER_CONFIG_FILE = ".fakeSMTP.properties";

	private final List<String> userSettingsKeys;

	private final Properties config = new Properties();

	public static class Settings {
		public static final String SMTP_DEFAULT_PORT = "smtp.default.port";
		public static final String EMAILS_DEFAULT_DIR = "emails.default.dir";
	}

	/**
	 * Opens the "{@code configuration.properties}" file and maps data.
	 */
	private Configuration() {
		userSettingsKeys = List.of(SMTP_DEFAULT_PORT, EMAILS_DEFAULT_DIR);

		try (InputStream in = getClass().getResourceAsStream(CONFIG_FILE)) {
			// Load defaults settings
			config.load(in);
			// and override them from user settings
			Properties userConfig = loadFromUserProfile();
			// merge only necessary properties
			userConfig.entrySet().stream()
					.filter(this::authorizedUserConfig)
					.forEach(entry -> config.put(entry.getKey(), entry.getValue()));
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public static Configuration getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns merged properties (application and user).
	 */
	public Properties getAllProperties() {
		return this.config;
	}

	public Properties getUserProperties() {
		final Properties userConfig = new Properties();
		this.config.entrySet().stream()
				.filter(this::authorizedUserConfig)
				.forEach(entry -> userConfig.put(entry.getKey(), entry.getValue()));
		return userConfig;
	}

	private boolean authorizedUserConfig(Map.Entry<Object, Object> entry) {
		return userSettingsKeys.stream().anyMatch(s -> s.equals(entry.getKey()));
	}

	/**
	 * Returns the value of a specific entry from the "{@code configuration.properties}" file.
	 *
	 * @param key a string representing the key from a key/value couple.
	 * @return the value of the key, or an empty string if the key was not found.
	 */
	public String get(String key) {
		if (config.containsKey(key)) {
			return config.getProperty(key);
		}
		return "";
	}

	/**
	 * Sets the value of a specific entry.
	 *
	 * @param key a string representing the key from a key/value couple.
	 * @param value the value of the key.
	 */
	public void set(String key, String value) {
		config.setProperty(key, value);
	}

	/**
	 * Saves configuration to file.
	 *
	 * @param file file to save configuration.
	 * @throws IOException
	 */
	public void saveToFile(File file) throws IOException {
		saveToFile(file, this.config);
	}

	private void saveToFile(File file, Properties config) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			config.store(fos, "Last user settings");
		}
	}

	/**
	 * Saves configuration to the {@code .fakesmtp.properties} file in user profile directory.
	 * Calls {@link Configuration#saveToFile(java.io.File)}.
	 *
	 * @throws IOException
	 */
	public void saveToUserProfile() throws IOException {
		Properties userConfig = new Properties();
		userSettingsKeys.forEach(key -> {
			if (this.config.get(key) != null) {
				userConfig.put(key, this.config.get(key));
			}
		});
		saveToFile(new File(System.getProperty("user.home"), USER_CONFIG_FILE), userConfig);
	}

	/**
	 * Loads configuration from file.
	 *
	 * @param file file to load configuration.
	 * @return INSTANCE.
	 * @throws IOException
	 */
	Properties loadFromFile(File file) throws IOException {
		Properties props = new Properties();
		if (file.exists() && file.canRead()) {
			try (FileInputStream fis = new FileInputStream(file)) {
				props.load(fis);
			}
		}
		return props;
	}

	/**
	 * Loads configuration from the .fakesmtp.properties file in user profile directory.
	 * Calls {@link Configuration#loadFromFile(java.io.File)}.
	 *
	 * @return INSTANCE.
	 * @throws IOException
	 */
	Properties loadFromUserProfile() throws IOException {
		return loadFromFile(new File(System.getProperty("user.home"), USER_CONFIG_FILE));
	}
}

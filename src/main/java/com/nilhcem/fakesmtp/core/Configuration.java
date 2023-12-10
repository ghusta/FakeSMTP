package com.nilhcem.fakesmtp.core;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

	private final Properties config = new Properties();

	/**
	 * Opens the "{@code configuration.properties}" file and maps data.
	 */
	private Configuration() {
		try (InputStream in = getClass().getResourceAsStream(CONFIG_FILE)) {
			// Load defaults settings
			config.load(in);
			// and override them from user settings
			loadFromUserProfile();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public static Configuration getInstance() {
		return INSTANCE;
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
		saveToFile(new File(System.getProperty("user.home"), USER_CONFIG_FILE));
	}

	/**
	 * Loads configuration from file.
	 *
	 * @param file file to load configuration.
	 * @return INSTANCE.
	 * @throws IOException
	 */
	public Configuration loadFromFile(File file) throws IOException {
		if (file.exists() && file.canRead()) {
			try (FileInputStream fis = new FileInputStream(file)) {
				config.load(fis);
			}
		}
		return this;
	}

	/**
	 * Loads configuration from the .fakesmtp.properties file in user profile directory.
	 * Calls {@link Configuration#loadFromFile(java.io.File)}.
	 *
	 * @return INSTANCE.
	 * @throws IOException
	 */
	public Configuration loadFromUserProfile() throws IOException {
		return loadFromFile(new File(System.getProperty("user.home"), USER_CONFIG_FILE));
	}
}

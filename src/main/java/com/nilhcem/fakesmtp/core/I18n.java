package com.nilhcem.fakesmtp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Initializes resource bundle and get messages from keys.
 * <p>
 * This class will be instantiated only once and will use the JVM's default locale.
 * </p>
 *
 * @author Nilhcem
 * @since 1.0
 */
public enum I18n {

	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger(I18n.class);

	private static final String RESOURCE_FILE = "i18n/messages";
	private final ResourceBundle resources;

	/**
	 * Initializes resource bundle with the JVM's default locale.
	 * <p>
	 * If the JVM's default locale doesn't have any resource file, will take the <code>en</code> resources instead.
	 * </p>
	 */
	I18n() {
		ResourceBundle bundle = ResourceBundle.getBundle(I18n.RESOURCE_FILE, Locale.getDefault());
		Objects.requireNonNull(bundle);
		resources = bundle;
	}

	/**
	 * Returns the resource for the key passed in parameters.
	 * <p>
	 * If the key is not found, returns an empty string.
	 * </p>
	 *
	 * @param key a String representing the key we want to get the resource from.
	 * @return The text corresponding to the key passed in parameters, or an empty string if not found.
	 */
	public String get(String key) {
		try {
			return resources.getString(key);
		} catch (MissingResourceException e) {
			log.error("{}", e.getMessage());
			return "";
		}
	}
}

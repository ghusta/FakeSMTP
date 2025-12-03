package com.nilhcem.fakesmtp.core;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class I18nTest {
	private static Locale defaultLocale;

	@BeforeAll
	static void initLocale() {
		defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.TAIWAN);
	}

	@AfterAll
	static void resetLocale() {
		Locale.setDefault(defaultLocale);
	}

	@Test
	void uniqueInstance() {
		I18n a = I18n.INSTANCE;
		I18n b = I18n.INSTANCE;
		assertThat(b).isSameAs(a);
	}

	@Test
	void getEmptyValueWhenKeyIsNotFound() {
		assertThat(I18n.INSTANCE.get("this.key.doesnt.exist")).isEmpty();
	}

	@Test
	void getValueWhenKeyIsFound() {
		assertThat(I18n.INSTANCE.get("menubar.file")).isNotEmpty();
	}
}

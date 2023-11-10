package com.nilhcem.fakesmtp.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {
	@Test
	void uniqueInstance() {
		Configuration a = Configuration.INSTANCE;
		Configuration b = Configuration.INSTANCE;
		assertThat(b).isSameAs(a);
	}

	@Test
	void getEmptyValueWhenKeyIsNotFound() {
		assertThat(Configuration.INSTANCE.get("this.key.doesnt.exist")).isEmpty();
	}

	@Test
	void getValueWhenKeyIsFound() {
		assertThat(Configuration.INSTANCE.get("application.name")).isNotEmpty();
	}
}

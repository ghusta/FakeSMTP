package com.nilhcem.fakesmtp.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {
	@Test
	void uniqueInstance() {
		Configuration a = Configuration.getInstance();
		Configuration b = Configuration.getInstance();
		assertThat(b).isSameAs(a);
	}

	@Test
	void getEmptyValueWhenKeyIsNotFound() {
		assertThat(Configuration.getInstance().get("this.key.doesnt.exist")).isEmpty();
	}

	@Test
	void getValueWhenKeyIsFound() {
		assertThat(Configuration.getInstance().get("application.name")).isNotEmpty();
	}
}

package com.nilhcem.fakesmtp.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationTest {
	@Test
	void uniqueInstance() {
		Configuration a = Configuration.INSTANCE;
		Configuration b = Configuration.INSTANCE;
		assertSame(a, b);
	}

	@Test
	void getEmptyValueWhenKeyIsNotFound() {
		assertTrue(Configuration.INSTANCE.get("this.key.doesnt.exist").isEmpty());
	}

	@Test
	void getValueWhenKeyIsFound() {
		assertFalse(Configuration.INSTANCE.get("application.name").isEmpty());
	}
}

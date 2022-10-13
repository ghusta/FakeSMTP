package com.nilhcem.fakesmtp.model;

import com.nilhcem.fakesmtp.core.exception.BindPortException;
import com.nilhcem.fakesmtp.core.exception.InvalidHostException;
import com.nilhcem.fakesmtp.core.exception.InvalidPortException;
import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;
import com.nilhcem.fakesmtp.core.test.TestConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIModelTest {
	@Test
	void uniqueInstance() {
		UIModel a = UIModel.INSTANCE;
		UIModel b = UIModel.INSTANCE;
		assertSame(a, b);
	}

	@Test
	void shouldHaveZeroMsgReceivedFirst() {
		assertEquals(0, UIModel.INSTANCE.getNbMessageReceived());
	}

	@Test
	void testInvalidPort() {
		assertThrows(InvalidPortException.class, () -> {
			UIModel.INSTANCE.setPort("INVALID");
			UIModel.INSTANCE.toggleButton();
		});
	}

	@Test
	void testInvalidHost() {
		assertThrows(InvalidHostException.class, () -> {
			UIModel.INSTANCE.setHost("INVALID");
			UIModel.INSTANCE.toggleButton();
		});
	}

	@Test
	void testIsStarted() throws BindPortException, OutOfRangePortException, InvalidPortException, InvalidHostException {
		UIModel.INSTANCE.setPort(Integer.toString(TestConfig.PORT_UNIT_TESTS));
		assertFalse(UIModel.INSTANCE.isStarted());

		UIModel.INSTANCE.toggleButton();
		assertTrue(UIModel.INSTANCE.isStarted());

		UIModel.INSTANCE.toggleButton();
		assertFalse(UIModel.INSTANCE.isStarted());
	}
}

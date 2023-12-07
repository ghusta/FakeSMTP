package com.nilhcem.fakesmtp.model;

import com.nilhcem.fakesmtp.core.exception.BindPortException;
import com.nilhcem.fakesmtp.core.exception.InvalidHostException;
import com.nilhcem.fakesmtp.core.exception.InvalidPortException;
import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;
import com.nilhcem.fakesmtp.core.test.TestConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class UIModelTest {
	@Test
	void uniqueInstance() {
		UIModel a = UIModel.INSTANCE;
		UIModel b = UIModel.INSTANCE;
		assertThat(b).isSameAs(a);
	}

	@Test
	void shouldHaveZeroMsgReceivedFirst() {
		assertThat(UIModel.INSTANCE.getNbMessageReceived()).isEqualTo(0);
	}

	@Test
	void testInvalidPort() {
		assertThatExceptionOfType(InvalidPortException.class).isThrownBy(() -> {
			UIModel.INSTANCE.setPort("INVALID");
			UIModel.INSTANCE.toggleButton();
		});
	}

	@Test
	void testInvalidHost() {
		assertThatExceptionOfType(InvalidHostException.class).isThrownBy(() -> {
			UIModel.INSTANCE.setHost("INVALID");
			UIModel.INSTANCE.toggleButton();
		});
	}

	@Test
	void testIsStarted() throws BindPortException, OutOfRangePortException, InvalidPortException, InvalidHostException {
		UIModel.INSTANCE.setPort(Integer.toString(TestConfig.PORT_UNIT_TESTS));
		assertThat(UIModel.INSTANCE.isServerStarted()).isFalse();

		UIModel.INSTANCE.toggleButton();
		assertThat(UIModel.INSTANCE.isServerStarted()).isTrue();

		UIModel.INSTANCE.toggleButton();
		assertThat(UIModel.INSTANCE.isServerStarted()).isFalse();
	}
}

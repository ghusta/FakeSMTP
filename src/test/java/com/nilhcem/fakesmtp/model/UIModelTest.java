package com.nilhcem.fakesmtp.model;

import com.nilhcem.fakesmtp.core.exception.InvalidHostException;
import com.nilhcem.fakesmtp.core.exception.InvalidPortException;
import com.nilhcem.fakesmtp.core.test.TestConfig;
import com.nilhcem.fakesmtp.gui.info.StartServerButton;
import org.junit.jupiter.api.Disabled;
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
		assertThat(UIModel.INSTANCE.getNbMessageReceived()).isZero();
	}

	@Test
	@Disabled("No Swing GUI for tests")
	void testInvalidPort() {
		assertThatExceptionOfType(InvalidPortException.class).isThrownBy(() -> {
			UIModel.INSTANCE.setPort("INVALID");
			StartServerButton startServerButton = new StartServerButton();
			startServerButton.toggleButton();
		});
	}

	@Test
	@Disabled("No Swing GUI for tests")
	void testInvalidHost() {
		assertThatExceptionOfType(InvalidHostException.class).isThrownBy(() -> {
			UIModel.INSTANCE.setHost("INVALID");
			StartServerButton startServerButton = new StartServerButton();
			startServerButton.toggleButton();
		});
	}

	@Test
	void testIsStarted() {
		UIModel.INSTANCE.setPort(Integer.toString(TestConfig.PORT_UNIT_TESTS));
		assertThat(UIModel.INSTANCE.isServerStarted()).isFalse();

		StartServerButton startServerButton = new StartServerButton();
		startServerButton.toggleButton();
		assertThat(UIModel.INSTANCE.isServerStarted()).isTrue();

		startServerButton.toggleButton();
		assertThat(UIModel.INSTANCE.isServerStarted()).isFalse();
	}
}

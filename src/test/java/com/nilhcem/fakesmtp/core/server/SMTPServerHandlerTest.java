package com.nilhcem.fakesmtp.core.server;

import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class SMTPServerHandlerTest {
	@Test
	void uniqueInstance() {
		SMTPServerHandler a = SMTPServerHandler.INSTANCE;
		SMTPServerHandler b = SMTPServerHandler.INSTANCE;
		assertThat(b).isSameAs(a);
	}

	@Test
	void testOutOfRangePort() {
		assertThatExceptionOfType(OutOfRangePortException.class).isThrownBy(() -> SMTPServerHandler.INSTANCE.startServer(9999999, null));
	}

	@Test
	void stopShouldDoNothingIfServerIsAlreadyStopped() {
		SMTPServerHandler.INSTANCE.stopServer();
		SMTPServerHandler.INSTANCE.stopServer();
		SMTPServerHandler.INSTANCE.stopServer();
	}
}

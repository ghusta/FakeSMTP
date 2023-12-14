package com.nilhcem.fakesmtp.log;

import java.util.Observable;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Logback appender class, which will redirect all logs to the {@code LogsPane} object.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class SMTPLogsAppender extends AppenderBase<ILoggingEvent> {

	private SMTPLogsObservable observable = new SMTPLogsObservable();

	/**
	 * Receives a log from Logback, and sends it to the {@code LogsPane} object.
	 *
	 * @param loggingEvent a Logback {@code ILoggingEvent} event.
	 */
	@Override
	protected void append(ILoggingEvent loggingEvent) {
		observable.notifyObservers(loggingEvent.getFormattedMessage());
	}

	/**
	 * Returns the log observable object.
	 *
	 * @return the log observable object.
	 */
	public Observable getObservable() {
		return observable;
	}
}

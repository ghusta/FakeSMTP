package com.nilhcem.fakesmtp.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class SmtpLogPublisherAppender extends AppenderBase<ILoggingEvent> {

    private final SubmissionPublisher<ILoggingEvent> smtpLogEventPublisher;

    public SmtpLogPublisherAppender() {
        // can pass Executor as arg
        this.smtpLogEventPublisher = new SubmissionPublisher<>();
    }

    public Flow.Publisher<ILoggingEvent> getSmtpLogEventPublisher() {
        return smtpLogEventPublisher;
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        smtpLogEventPublisher.submit(iLoggingEvent);
    }

}

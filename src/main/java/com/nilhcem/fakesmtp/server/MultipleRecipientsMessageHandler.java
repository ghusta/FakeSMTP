package com.nilhcem.fakesmtp.server;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultipleRecipientsMessageHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(MultipleRecipientsMessageHandler.class);

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final MailSaver saver;

    private String from = "";
    private final List<String> recipients = new ArrayList<>();
    private String content = "";

    public MultipleRecipientsMessageHandler(MailSaver saver) {
        Objects.requireNonNull(saver);
        this.saver = saver;
    }

    @Override
    public void from(String from) throws RejectException {
        Objects.requireNonNull(from);
        this.from = from;
    }

    @Override
    public void recipient(String recipient) {
        Objects.requireNonNull(recipient);
        this.recipients.add(recipient);
    }

    @Override
    @Nullable
    public String data(InputStream data) throws RejectException, IOException {
        content = convertStreamToString(data);
        return null;
    }

    @Override
    public void done() {
        // do what you like with the message
        saver.saveEmailAndNotify(from, recipients, content);
    }

    private static String convertStreamToString(InputStream is) {
        // see: org.subethamail.smtp.internal.io.ReceivedHeaderStream
        // see: org.subethamail.smtp.server.SMTPServer.getDisableReceivedHeaders
        // and: org.subethamail.smtp.server.SMTPServer.Builder.insertReceivedHeaders(boolean)
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(LINE_SEPARATOR);
            }
        } catch (IOException e) {
            log.error("", e);
        }
        return sb.toString();
    }
}

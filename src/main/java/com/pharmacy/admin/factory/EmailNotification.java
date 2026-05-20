package com.pharmacy.admin.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotification implements Notification {

    private static final Logger log = LoggerFactory.getLogger(EmailNotification.class);

    private final String recipient;
    private final String message;

    public EmailNotification(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
    }

    @Override
    public void send() {
        log.info("Sending EMAIL to {}: {}", recipient, message);
    }
}

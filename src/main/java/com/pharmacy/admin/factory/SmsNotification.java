package com.pharmacy.admin.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsNotification implements Notification {

    private static final Logger log = LoggerFactory.getLogger(SmsNotification.class);

    private final String recipient;
    private final String message;

    public SmsNotification(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
    }

    @Override
    public void send() {
        log.info("Sending SMS to {}: {}", recipient, message);
    }
}

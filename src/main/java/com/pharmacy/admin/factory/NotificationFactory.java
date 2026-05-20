package com.pharmacy.admin.factory;

public class NotificationFactory {

    private NotificationFactory() {
        // Utility class — no instantiation
    }

    /**
     * Factory Method: creates the appropriate Notification implementation.
     *
     * @param type      "EMAIL" returns an EmailNotification; any other value returns SmsNotification
     * @param recipient target address (email or phone)
     * @param message   notification body
     * @return concrete Notification instance
     * @throws IllegalArgumentException if recipient or message is null
     */
    public static Notification createNotification(String type, String recipient, String message) {
        if (recipient == null || message == null) {
            throw new IllegalArgumentException("Notification recipient and message must not be null");
        }
        if ("EMAIL".equalsIgnoreCase(type)) {
            return new EmailNotification(recipient, message);
        }
        return new SmsNotification(recipient, message);
    }
}

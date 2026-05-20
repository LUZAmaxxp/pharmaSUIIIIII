package com.pharmacy.admin;

import com.pharmacy.admin.factory.EmailNotification;
import com.pharmacy.admin.factory.Notification;
import com.pharmacy.admin.factory.NotificationFactory;
import com.pharmacy.admin.factory.SmsNotification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NotificationFactoryTest {

    @Test
    void createNotification_shouldReturnEmailNotification_forEmailType() {
        Notification notification = NotificationFactory.createNotification("EMAIL", "test@mail.com", "Hello");
        assertThat(notification).isInstanceOf(EmailNotification.class);
    }

    @Test
    void createNotification_shouldReturnSmsNotification_forUnknownType() {
        Notification notification = NotificationFactory.createNotification("SMS", "+212600000000", "Hello");
        assertThat(notification).isInstanceOf(SmsNotification.class);
    }

    @Test
    void createNotification_shouldBeCaseInsensitive_forEmailType() {
        Notification notification = NotificationFactory.createNotification("email", "test@mail.com", "Hello");
        assertThat(notification).isInstanceOf(EmailNotification.class);
    }

    @Test
    void createNotification_shouldThrowIllegalArgumentException_whenRecipientIsNull() {
        assertThatThrownBy(() -> NotificationFactory.createNotification("EMAIL", null, "Hello"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recipient");
    }

    @Test
    void createNotification_shouldThrowIllegalArgumentException_whenMessageIsNull() {
        assertThatThrownBy(() -> NotificationFactory.createNotification("EMAIL", "test@mail.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("message");
    }
}

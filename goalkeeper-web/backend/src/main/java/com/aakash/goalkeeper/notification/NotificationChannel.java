package com.aakash.goalkeeper.notification;

/**
 * Delivery for a notification outside the in-app feed (email today; a Kafka-backed
 * consumer can implement this later without touching the caller).
 */
public interface NotificationChannel {
    void send(String toEmail, String subject, String body);
}

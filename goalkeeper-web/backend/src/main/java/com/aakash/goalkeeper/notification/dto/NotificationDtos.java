package com.aakash.goalkeeper.notification.dto;

import com.aakash.goalkeeper.notification.NotificationType;

import java.time.Instant;
import java.util.UUID;

public class NotificationDtos {

    public record NotificationDto(
            UUID id,
            UUID goalId,
            NotificationType type,
            String title,
            String body,
            boolean read,
            Instant createdAt
    ) {}

    public record UnreadCount(long count) {}
}

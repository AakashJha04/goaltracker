package com.aakash.goalkeeper.notification;

import com.aakash.goalkeeper.common.ApiException;
import com.aakash.goalkeeper.notification.dto.NotificationDtos.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    public Page<NotificationDto> list(UUID userId, Pageable pageable) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toDto);
    }

    public long unreadCount(UUID userId) {
        return notifications.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public Notification create(UUID userId, UUID goalId, NotificationType type, String title, String body) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setGoalId(goalId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        return notifications.save(n);
    }

    @Transactional
    public NotificationDto markRead(UUID userId, UUID id) {
        Notification n = notifications.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        n.setRead(true);
        notifications.save(n);
        return toDto(n);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notifications.markAllRead(userId);
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(n.getId(), n.getGoalId(), n.getType(), n.getTitle(), n.getBody(),
                n.isRead(), n.getCreatedAt());
    }
}

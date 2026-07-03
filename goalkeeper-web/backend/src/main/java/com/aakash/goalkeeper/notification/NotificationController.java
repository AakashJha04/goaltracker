package com.aakash.goalkeeper.notification;

import com.aakash.goalkeeper.goal.dto.PageResponse;
import com.aakash.goalkeeper.notification.dto.NotificationDtos.NotificationDto;
import com.aakash.goalkeeper.notification.dto.NotificationDtos.UnreadCount;
import com.aakash.goalkeeper.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<NotificationDto> list(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        int boundedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var result = service.list(principal.id(), PageRequest.of(Math.max(page, 0), boundedSize));
        return PageResponse.of(result, dto -> dto);
    }

    @GetMapping("/unread-count")
    public UnreadCount unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return new UnreadCount(service.unreadCount(principal.id()));
    }

    @PatchMapping("/{id}/read")
    public NotificationDto markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return service.markRead(principal.id(), id);
    }

    @PostMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        service.markAllRead(principal.id());
    }
}

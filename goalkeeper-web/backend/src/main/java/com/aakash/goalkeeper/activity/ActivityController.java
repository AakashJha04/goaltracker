package com.aakash.goalkeeper.activity;

import com.aakash.goalkeeper.activity.dto.ActivityDtos.ActivityDto;
import com.aakash.goalkeeper.goal.dto.PageResponse;
import com.aakash.goalkeeper.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ActivityController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @GetMapping("/api/goals/{goalId}/activity")
    public PageResponse<ActivityDto> list(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        int boundedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var result = service.list(principal.id(), goalId, PageRequest.of(Math.max(page, 0), boundedSize));
        return PageResponse.of(result, dto -> dto);
    }
}

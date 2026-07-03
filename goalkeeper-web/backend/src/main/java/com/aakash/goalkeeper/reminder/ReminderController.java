package com.aakash.goalkeeper.reminder;

import com.aakash.goalkeeper.reminder.dto.ReminderDtos.*;
import com.aakash.goalkeeper.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ReminderController {

    private final ReminderService service;

    public ReminderController(ReminderService service) {
        this.service = service;
    }

    @GetMapping("/api/goals/{goalId}/reminders")
    public List<ReminderDto> list(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId) {
        return service.list(principal.id(), goalId);
    }

    @PostMapping("/api/goals/{goalId}/reminders")
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderDto create(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId,
                               @Valid @RequestBody ReminderCreateRequest req) {
        return service.create(principal.id(), goalId, req);
    }

    @DeleteMapping("/api/goals/{goalId}/reminders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId, @PathVariable UUID id) {
        service.delete(principal.id(), goalId, id);
    }
}

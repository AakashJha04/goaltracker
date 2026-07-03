package com.aakash.goalkeeper.reminder.dto;

import com.aakash.goalkeeper.reminder.ReminderChannel;
import com.aakash.goalkeeper.reminder.ReminderStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public class ReminderDtos {

    public record ReminderCreateRequest(
            @NotNull @Future Instant remindAt,
            ReminderChannel channel
    ) {}

    public record ReminderDto(UUID id, Instant remindAt, ReminderChannel channel, ReminderStatus status) {}
}

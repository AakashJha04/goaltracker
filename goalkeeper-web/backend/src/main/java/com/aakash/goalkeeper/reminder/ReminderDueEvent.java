package com.aakash.goalkeeper.reminder;

import java.util.UUID;

/** Published after a reminder is locked and marked SENT by the scheduler. */
public record ReminderDueEvent(UUID reminderId, UUID goalId) {}

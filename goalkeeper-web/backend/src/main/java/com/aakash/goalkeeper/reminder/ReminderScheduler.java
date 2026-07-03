package com.aakash.goalkeeper.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final ReminderService reminders;

    public ReminderScheduler(ReminderService reminders) {
        this.reminders = reminders;
    }

    @Scheduled(fixedRate = 60_000)
    public void dispatchDueReminders() {
        try {
            reminders.dispatchDueReminders();
        } catch (Exception e) {
            // One bad batch shouldn't stop future runs.
            log.error("Reminder dispatch failed", e);
        }
    }
}

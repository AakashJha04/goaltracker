package com.aakash.goalkeeper.reminder;

import com.aakash.goalkeeper.common.ApiException;
import com.aakash.goalkeeper.goal.GoalService;
import com.aakash.goalkeeper.reminder.dto.ReminderDtos.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReminderService {

    private static final int MAX_PER_GOAL = 3;
    private static final int BATCH_SIZE = 50;

    private final ReminderRepository reminders;
    private final GoalService goals;
    private final ApplicationEventPublisher events;

    public ReminderService(ReminderRepository reminders, GoalService goals, ApplicationEventPublisher events) {
        this.reminders = reminders;
        this.goals = goals;
        this.events = events;
    }

    public List<ReminderDto> list(UUID userId, UUID goalId) {
        goals.requireOwned(userId, goalId);
        return reminders.findByGoalIdOrderByRemindAtAsc(goalId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ReminderDto create(UUID userId, UUID goalId, ReminderCreateRequest req) {
        goals.requireOwned(userId, goalId);
        long count = reminders.countByGoalIdAndStatus(goalId, ReminderStatus.PENDING);
        if (count >= MAX_PER_GOAL) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A goal can have at most " + MAX_PER_GOAL + " reminders");
        }
        Reminder r = new Reminder();
        r.setGoalId(goalId);
        r.setRemindAt(req.remindAt());
        r.setChannel(req.channel() != null ? req.channel() : ReminderChannel.IN_APP);
        reminders.save(r);
        return toDto(r);
    }

    @Transactional
    public void delete(UUID userId, UUID goalId, UUID reminderId) {
        goals.requireOwned(userId, goalId);
        Reminder r = reminders.findByIdAndGoalId(reminderId, goalId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reminder not found"));
        reminders.delete(r);
    }

    /**
     * Locks and marks due reminders SENT, then publishes one event per reminder for the
     * async listener to turn into a notification (and email, if configured). Runs in its
     * own transaction so SKIP LOCKED plays nicely with concurrent scheduler instances.
     */
    @Transactional
    public void dispatchDueReminders() {
        List<Reminder> due = reminders.lockDueReminders(BATCH_SIZE);
        for (Reminder r : due) {
            r.setStatus(ReminderStatus.SENT);
            reminders.save(r);
            events.publishEvent(new ReminderDueEvent(r.getId(), r.getGoalId()));
        }
    }

    private ReminderDto toDto(Reminder r) {
        return new ReminderDto(r.getId(), r.getRemindAt(), r.getChannel(), r.getStatus());
    }
}

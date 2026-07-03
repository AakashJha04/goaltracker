package com.aakash.goalkeeper.notification;

import com.aakash.goalkeeper.goal.Goal;
import com.aakash.goalkeeper.goal.GoalRepository;
import com.aakash.goalkeeper.reminder.Reminder;
import com.aakash.goalkeeper.reminder.ReminderChannel;
import com.aakash.goalkeeper.reminder.ReminderDueEvent;
import com.aakash.goalkeeper.reminder.ReminderRepository;
import com.aakash.goalkeeper.user.User;
import com.aakash.goalkeeper.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Turns a due reminder into an in-app notification, and an email if the reminder asked
 * for one. Runs async, after the reminder's own transaction commits, so a slow email
 * send never blocks the scheduler.
 */
@Component
public class ReminderEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReminderEventListener.class);

    private final ReminderRepository reminders;
    private final GoalRepository goals;
    private final UserRepository users;
    private final NotificationService notifications;
    private final NotificationChannel channel;

    public ReminderEventListener(ReminderRepository reminders, GoalRepository goals, UserRepository users,
                                  NotificationService notifications, NotificationChannel channel) {
        this.reminders = reminders;
        this.goals = goals;
        this.users = users;
        this.notifications = notifications;
        this.channel = channel;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReminderDue(ReminderDueEvent event) {
        Reminder reminder = reminders.findById(event.reminderId()).orElse(null);
        Goal goal = goals.findById(event.goalId()).orElse(null);
        if (reminder == null || goal == null) {
            log.warn("Reminder or goal disappeared before dispatch: {}", event);
            return;
        }
        User user = users.findById(goal.getUserId()).orElse(null);
        if (user == null) return;

        String title = "Reminder: " + goal.getTitle();
        String body = "You asked to be reminded about \"" + goal.getTitle() + "\".";
        notifications.create(user.getId(), goal.getId(), NotificationType.REMINDER, title, body);

        if (reminder.getChannel() == ReminderChannel.EMAIL) {
            channel.send(user.getEmail(), title, body);
        }
    }
}

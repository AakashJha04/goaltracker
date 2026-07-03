package com.aakash.goalkeeper.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByGoalIdOrderByRemindAtAsc(UUID goalId);

    Optional<Reminder> findByIdAndGoalId(UUID id, UUID goalId);

    long countByGoalIdAndStatus(UUID goalId, ReminderStatus status);

    /**
     * Locks due, still-pending reminders so concurrent scheduler instances never send the
     * same reminder twice; skips rows another instance already has locked.
     */
    @Query(value = "SELECT * FROM reminders WHERE status = 'PENDING' AND remind_at <= now() " +
            "ORDER BY remind_at ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Reminder> lockDueReminders(@Param("limit") int limit);
}

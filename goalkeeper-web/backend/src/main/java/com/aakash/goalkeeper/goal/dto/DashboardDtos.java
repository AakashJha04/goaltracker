package com.aakash.goalkeeper.goal.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DashboardDtos {

    // Serializable so this can also be stored in Redis when app.cache.type=redis.
    public record UpcomingGoal(UUID id, String title, Instant targetDate) implements Serializable {}

    public record DashboardStats(
            long totalGoals,
            long activeGoals,
            long completedGoals,
            long archivedGoals,
            double completionRate,
            List<UpcomingGoal> upcomingDeadlines,
            Map<String, Long> categoryBreakdown
    ) implements Serializable {}
}

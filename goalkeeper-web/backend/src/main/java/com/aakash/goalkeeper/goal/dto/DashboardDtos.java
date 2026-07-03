package com.aakash.goalkeeper.goal.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DashboardDtos {

    public record UpcomingGoal(UUID id, String title, Instant targetDate) {}

    public record DashboardStats(
            long totalGoals,
            long activeGoals,
            long completedGoals,
            long archivedGoals,
            double completionRate,
            List<UpcomingGoal> upcomingDeadlines,
            Map<String, Long> categoryBreakdown
    ) {}
}

package com.aakash.goalkeeper.goal;

import com.aakash.goalkeeper.goal.dto.DashboardDtos.DashboardStats;
import com.aakash.goalkeeper.goal.dto.DashboardDtos.UpcomingGoal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DashboardService {

    public static final String CACHE_NAME = "dashboardStats";
    private static final int UPCOMING_LIMIT = 5;

    private final GoalRepository goals;

    public DashboardService(GoalRepository goals) {
        this.goals = goals;
    }

    @Cacheable(cacheNames = CACHE_NAME, key = "#userId")
    public DashboardStats stats(UUID userId) {
        long total = goals.countByUserIdAndDeletedFalse(userId);
        long active = goals.countByUserIdAndDeletedFalseAndStatus(userId, GoalStatus.ACTIVE);
        long completed = goals.countByUserIdAndDeletedFalseAndStatus(userId, GoalStatus.COMPLETED);
        long archived = goals.countByUserIdAndDeletedFalseAndStatus(userId, GoalStatus.ARCHIVED);
        double completionRate = total == 0 ? 0.0 : Math.round((completed * 1000.0 / total)) / 10.0;

        List<UpcomingGoal> upcoming = goals.findUpcomingDeadlines(userId, PageRequest.of(0, UPCOMING_LIMIT))
                .stream()
                .map(g -> new UpcomingGoal(g.getId(), g.getTitle(), g.getTargetDate()))
                .toList();

        Map<String, Long> categoryBreakdown = new LinkedHashMap<>();
        for (Object[] row : goals.categoryBreakdown(userId)) {
            categoryBreakdown.put((String) row[0], (Long) row[1]);
        }

        return new DashboardStats(total, active, completed, archived, completionRate, upcoming, categoryBreakdown);
    }
}

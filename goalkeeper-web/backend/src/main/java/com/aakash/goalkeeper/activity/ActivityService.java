package com.aakash.goalkeeper.activity;

import com.aakash.goalkeeper.activity.dto.ActivityDtos.ActivityDto;
import com.aakash.goalkeeper.common.ApiException;
import com.aakash.goalkeeper.goal.GoalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Depends on GoalRepository directly (not GoalService) so GoalService can depend on this
 * class to record events without creating a bean cycle.
 */
@Service
public class ActivityService {

    private final ActivityLogRepository repository;
    private final GoalRepository goals;

    public ActivityService(ActivityLogRepository repository, GoalRepository goals) {
        this.repository = repository;
        this.goals = goals;
    }

    @Transactional
    public void record(UUID goalId, ActivityType type, String description) {
        ActivityLog log = new ActivityLog();
        log.setGoalId(goalId);
        log.setType(type);
        log.setDescription(description);
        repository.save(log);
    }

    public Page<ActivityDto> list(UUID userId, UUID goalId, Pageable pageable) {
        goals.findByIdAndUserIdAndDeletedFalse(goalId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Goal not found"));
        return repository.findByGoalIdOrderByCreatedAtDesc(goalId, pageable)
                .map(a -> new ActivityDto(a.getId(), a.getType(), a.getDescription(), a.getCreatedAt()));
    }
}

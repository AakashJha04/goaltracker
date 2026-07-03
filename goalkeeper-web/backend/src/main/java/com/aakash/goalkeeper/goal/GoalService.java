package com.aakash.goalkeeper.goal;

import com.aakash.goalkeeper.common.ApiException;
import com.aakash.goalkeeper.goal.dto.GoalDtos.*;
import com.aakash.goalkeeper.goal.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GoalService {

    private final GoalRepository goals;

    public GoalService(GoalRepository goals) {
        this.goals = goals;
    }

    public PageResponse<GoalDto> list(UUID userId, GoalStatus status, String category, String search, Pageable pageable) {
        Specification<Goal> spec = Specification.where(GoalSpecifications.ownedBy(userId))
                .and(GoalSpecifications.notDeleted())
                .and(GoalSpecifications.statusEquals(status))
                .and(GoalSpecifications.categoryEquals(category))
                .and(GoalSpecifications.titleContains(search));
        Page<Goal> page = goals.findAll(spec, pageable);
        return PageResponse.of(page, this::toDto);
    }

    @Transactional
    public GoalDto create(UUID userId, GoalCreateRequest req) {
        Goal g = new Goal();
        g.setUserId(userId);
        g.setTitle(req.title().trim());
        g.setDescription(req.description());
        g.setCategory(req.category());
        g.setPriority(req.priority() != null ? req.priority() : GoalPriority.MEDIUM);
        g.setTargetDate(req.targetDate());
        goals.save(g);
        return toDto(g);
    }

    public GoalDto get(UUID userId, UUID goalId) {
        return toDto(requireOwned(userId, goalId));
    }

    @Transactional
    public GoalDto update(UUID userId, UUID goalId, GoalUpdateRequest req) {
        Goal g = requireOwned(userId, goalId);
        g.setTitle(req.title().trim());
        g.setDescription(req.description());
        g.setCategory(req.category());
        g.setPriority(req.priority());
        g.setTargetDate(req.targetDate());
        goals.save(g);
        return toDto(g);
    }

    @Transactional
    public void delete(UUID userId, UUID goalId) {
        Goal g = requireOwned(userId, goalId);
        g.setDeleted(true);
        goals.save(g);
    }

    @Transactional
    public GoalDto updateStatus(UUID userId, UUID goalId, GoalStatus status) {
        Goal g = requireOwned(userId, goalId);
        g.setStatus(status);
        if (status == GoalStatus.COMPLETED) g.setProgress(100);
        goals.save(g);
        return toDto(g);
    }

    @Transactional
    public GoalDto updateProgress(UUID userId, UUID goalId, int progress) {
        Goal g = requireOwned(userId, goalId);
        g.setProgress(progress);
        goals.save(g);
        return toDto(g);
    }

    /** Package-visible: used by MilestoneService to recompute progress and verify ownership. */
    Goal requireOwned(UUID userId, UUID goalId) {
        return goals.findByIdAndUserIdAndDeletedFalse(goalId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Goal not found"));
    }

    void save(Goal g) {
        goals.save(g);
    }

    GoalDto toDto(Goal g) {
        return new GoalDto(g.getId(), g.getTitle(), g.getDescription(), g.getCategory(),
                g.getPriority(), g.getStatus(), g.getProgress(), g.getTargetDate(),
                g.getCreatedAt(), g.getUpdatedAt());
    }
}

package com.aakash.goalkeeper.goal;

import com.aakash.goalkeeper.activity.ActivityService;
import com.aakash.goalkeeper.activity.ActivityType;
import com.aakash.goalkeeper.common.ApiException;
import com.aakash.goalkeeper.goal.dto.MilestoneDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MilestoneService {

    private static final int MAX_PER_GOAL = 50;

    private final GoalService goals;
    private final MilestoneRepository milestones;
    private final ActivityService activity;

    public MilestoneService(GoalService goals, MilestoneRepository milestones, ActivityService activity) {
        this.goals = goals;
        this.milestones = milestones;
        this.activity = activity;
    }

    public List<MilestoneDto> list(UUID userId, UUID goalId) {
        goals.requireOwned(userId, goalId);
        return milestones.findByGoalIdOrderByPositionAsc(goalId).stream().map(this::toDto).toList();
    }

    @Transactional
    public MilestoneDto create(UUID userId, UUID goalId, MilestoneCreateRequest req) {
        goals.requireOwned(userId, goalId);
        long count = milestones.countByGoalId(goalId);
        if (count >= MAX_PER_GOAL) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A goal can have at most " + MAX_PER_GOAL + " milestones");
        }
        Milestone m = new Milestone();
        m.setGoalId(goalId);
        m.setTitle(req.title().trim());
        m.setPosition((int) count);
        milestones.save(m);
        activity.record(goalId, ActivityType.MILESTONE_ADDED, "Milestone added: " + m.getTitle());
        return toDto(m);
    }

    @Transactional
    public MilestoneDto update(UUID userId, UUID milestoneId, MilestoneUpdateRequest req) {
        Milestone m = requireForUser(userId, milestoneId);
        m.setTitle(req.title().trim());
        boolean doneChanged = m.isDone() != req.done();
        m.setDone(req.done());
        milestones.save(m);
        if (doneChanged) {
            recomputeProgress(userId, m.getGoalId());
            String verb = req.done() ? "completed" : "reopened";
            activity.record(m.getGoalId(), ActivityType.MILESTONE_TOGGLED, "Milestone " + verb + ": " + m.getTitle());
        }
        return toDto(m);
    }

    @Transactional
    public void delete(UUID userId, UUID milestoneId) {
        Milestone m = requireForUser(userId, milestoneId);
        UUID goalId = m.getGoalId();
        milestones.delete(m);
        recomputeProgress(userId, goalId);
        activity.record(goalId, ActivityType.MILESTONE_REMOVED, "Milestone removed: " + m.getTitle());
    }

    @Transactional
    public void reorder(UUID userId, UUID goalId, List<UUID> orderedIds) {
        goals.requireOwned(userId, goalId);
        List<Milestone> current = milestones.findByGoalIdOrderByPositionAsc(goalId);
        Map<UUID, Milestone> byId = current.stream()
                .collect(java.util.stream.Collectors.toMap(Milestone::getId, m -> m));
        if (byId.size() != orderedIds.size() || !byId.keySet().containsAll(orderedIds)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reorder list must include every milestone exactly once");
        }
        for (int i = 0; i < orderedIds.size(); i++) {
            Milestone m = byId.get(orderedIds.get(i));
            m.setPosition(i);
            milestones.save(m);
        }
    }

    private void recomputeProgress(UUID userId, UUID goalId) {
        long total = milestones.countByGoalId(goalId);
        if (total == 0) return;
        long done = milestones.countByGoalIdAndDoneTrue(goalId);
        int progress = (int) Math.round(done * 100.0 / total);
        Goal g = goals.requireOwned(userId, goalId);
        g.setProgress(progress);
        goals.save(g);
    }

    private Milestone requireForUser(UUID userId, UUID milestoneId) {
        Milestone m = milestones.findById(milestoneId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Milestone not found"));
        goals.requireOwned(userId, m.getGoalId()); // throws if the goal isn't the caller's
        return m;
    }

    private MilestoneDto toDto(Milestone m) {
        return new MilestoneDto(m.getId(), m.getTitle(), m.isDone(), m.getPosition());
    }
}

package com.aakash.goalkeeper.goal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    List<Milestone> findByGoalIdOrderByPositionAsc(UUID goalId);

    Optional<Milestone> findByIdAndGoalId(UUID id, UUID goalId);

    long countByGoalId(UUID goalId);

    long countByGoalIdAndDoneTrue(UUID goalId);
}

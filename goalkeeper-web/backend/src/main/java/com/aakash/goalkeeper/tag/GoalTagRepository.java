package com.aakash.goalkeeper.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GoalTagRepository extends JpaRepository<GoalTag, GoalTagId> {

    List<GoalTag> findByGoalId(UUID goalId);

    List<GoalTag> findByGoalIdIn(Collection<UUID> goalIds);

    boolean existsByGoalIdAndTagId(UUID goalId, UUID tagId);

    void deleteByGoalIdAndTagId(UUID goalId, UUID tagId);

    @Query("SELECT gt.goalId FROM GoalTag gt WHERE gt.tagId = :tagId")
    List<UUID> findGoalIdsByTagId(@Param("tagId") UUID tagId);
}

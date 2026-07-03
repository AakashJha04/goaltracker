package com.aakash.goalkeeper.goal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID>, JpaSpecificationExecutor<Goal> {

    Optional<Goal> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);

    long countByUserIdAndDeletedFalseAndStatus(UUID userId, GoalStatus status);

    long countByUserIdAndDeletedFalse(UUID userId);

    @Query("SELECT g FROM Goal g WHERE g.userId = :userId AND g.deleted = false " +
            "AND g.status <> com.aakash.goalkeeper.goal.GoalStatus.COMPLETED " +
            "AND g.targetDate IS NOT NULL ORDER BY g.targetDate ASC")
    List<Goal> findUpcomingDeadlines(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COALESCE(g.category, 'Uncategorized'), COUNT(g) FROM Goal g " +
            "WHERE g.userId = :userId AND g.deleted = false GROUP BY g.category")
    List<Object[]> categoryBreakdown(@Param("userId") UUID userId);
}

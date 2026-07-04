package com.aakash.goalkeeper.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "goal_tags")
@IdClass(GoalTagId.class)
public class GoalTag {

    @Id
    @Column(name = "goal_id")
    private UUID goalId;

    @Id
    @Column(name = "tag_id")
    private UUID tagId;

    protected GoalTag() {}

    public GoalTag(UUID goalId, UUID tagId) {
        this.goalId = goalId;
        this.tagId = tagId;
    }

    public UUID getGoalId() { return goalId; }
    public UUID getTagId() { return tagId; }
}

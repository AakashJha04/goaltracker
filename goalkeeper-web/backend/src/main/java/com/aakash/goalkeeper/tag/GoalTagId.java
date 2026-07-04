package com.aakash.goalkeeper.tag;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class GoalTagId implements Serializable {
    private UUID goalId;
    private UUID tagId;

    public GoalTagId() {}

    public GoalTagId(UUID goalId, UUID tagId) {
        this.goalId = goalId;
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoalTagId that)) return false;
        return Objects.equals(goalId, that.goalId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goalId, tagId);
    }
}

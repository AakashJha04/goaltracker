package com.aakash.goalkeeper.goal;

import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

/** Composable filters for the goal list query; every list is scoped to a single owner. */
class GoalSpecifications {

    static Specification<Goal> ownedBy(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    static Specification<Goal> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    static Specification<Goal> statusEquals(GoalStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    static Specification<Goal> categoryEquals(String category) {
        return (root, query, cb) -> (category == null || category.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    static Specification<Goal> titleContains(String search) {
        return (root, query, cb) -> (search == null || search.isBlank())
                ? null
                : cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%");
    }

    static Specification<Goal> idIn(Collection<UUID> ids) {
        return (root, query, cb) -> ids == null ? null : root.get("id").in(ids);
    }
}

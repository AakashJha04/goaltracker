package com.aakash.goalkeeper.goal;

import com.aakash.goalkeeper.AbstractIntegrationTest;
import com.aakash.goalkeeper.goal.dto.GoalDtos.*;
import com.aakash.goalkeeper.goal.dto.MilestoneDtos.MilestoneCreateRequest;
import com.aakash.goalkeeper.goal.dto.MilestoneDtos.MilestoneDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/** A user must never be able to read or mutate another user's goals or milestones. */
class GoalOwnershipTest extends AbstractIntegrationTest {

    @Test
    void otherUserCannotReadUpdateOrDeleteGoal() {
        String owner = registerAndGetToken();
        String intruder = registerAndGetToken();

        GoalDto goal = post("/api/goals", owner,
                new GoalCreateRequest("Private goal", null, null, GoalPriority.LOW, null),
                GoalDto.class).getBody();

        assertThat(get("/api/goals/" + goal.id(), intruder, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        GoalUpdateRequest update = new GoalUpdateRequest("Hijacked", null, null, GoalPriority.HIGH, null);
        assertThat(put("/api/goals/" + goal.id(), intruder, update, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(delete("/api/goals/" + goal.id(), intruder, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // The goal must be untouched from the owner's perspective.
        ResponseEntity<GoalDto> stillOwned = get("/api/goals/" + goal.id(), owner, GoalDto.class);
        assertThat(stillOwned.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stillOwned.getBody().title()).isEqualTo("Private goal");
    }

    @Test
    void otherUserCannotSeeOrMutateMilestones() {
        String owner = registerAndGetToken();
        String intruder = registerAndGetToken();

        GoalDto goal = post("/api/goals", owner,
                new GoalCreateRequest("Private goal", null, null, GoalPriority.LOW, null),
                GoalDto.class).getBody();
        MilestoneDto milestone = post("/api/goals/" + goal.id() + "/milestones", owner,
                new MilestoneCreateRequest("Secret step"), MilestoneDto.class).getBody();

        assertThat(get("/api/goals/" + goal.id() + "/milestones", intruder, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post("/api/goals/" + goal.id() + "/milestones", intruder,
                new MilestoneCreateRequest("Injected"), Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete("/api/milestones/" + milestone.id(), intruder, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void dashboardStatsAreScopedToTheCaller() {
        String owner = registerAndGetToken();
        String other = registerAndGetToken();

        post("/api/goals", owner, new GoalCreateRequest("Goal A", null, null, GoalPriority.LOW, null), Object.class);
        post("/api/goals", owner, new GoalCreateRequest("Goal B", null, null, GoalPriority.LOW, null), Object.class);

        var otherStats = get("/api/dashboard/stats", other,
                com.aakash.goalkeeper.goal.dto.DashboardDtos.DashboardStats.class).getBody();
        assertThat(otherStats.totalGoals()).isZero();

        var ownerStats = get("/api/dashboard/stats", owner,
                com.aakash.goalkeeper.goal.dto.DashboardDtos.DashboardStats.class).getBody();
        assertThat(ownerStats.totalGoals()).isEqualTo(2);
    }
}

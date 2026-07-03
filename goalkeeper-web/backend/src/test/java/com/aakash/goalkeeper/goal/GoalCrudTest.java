package com.aakash.goalkeeper.goal;

import com.aakash.goalkeeper.AbstractIntegrationTest;
import com.aakash.goalkeeper.goal.dto.GoalDtos.*;
import com.aakash.goalkeeper.goal.dto.MilestoneDtos.*;
import com.aakash.goalkeeper.goal.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoalCrudTest extends AbstractIntegrationTest {

    @Test
    void createListUpdateAndDeleteGoal() {
        String token = registerAndGetToken();

        GoalCreateRequest create = new GoalCreateRequest("Run a marathon", "26.2 miles", "Fitness",
                GoalPriority.HIGH, null);
        ResponseEntity<GoalDto> createRes = post("/api/goals", token, create, GoalDto.class);
        assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        GoalDto goal = createRes.getBody();
        assertThat(goal.title()).isEqualTo("Run a marathon");
        assertThat(goal.status()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(goal.progress()).isZero();

        ResponseEntity<PageResponse<GoalDto>> listRes = rest.exchange(
                baseUrl() + "/api/goals", HttpMethod.GET, new HttpEntity<>(authHeaders(token)),
                new ParameterizedTypeReference<PageResponse<GoalDto>>() {});
        assertThat(listRes.getBody().items()).extracting(GoalDto::id).contains(goal.id());

        GoalUpdateRequest update = new GoalUpdateRequest("Run a faster marathon", "sub-4:00", "Fitness",
                GoalPriority.HIGH, null);
        ResponseEntity<GoalDto> updateRes = put("/api/goals/" + goal.id(), token, update, GoalDto.class);
        assertThat(updateRes.getBody().title()).isEqualTo("Run a faster marathon");

        ResponseEntity<GoalDto> progressRes = patch("/api/goals/" + goal.id() + "/progress", token,
                new ProgressUpdateRequest(40), GoalDto.class);
        assertThat(progressRes.getBody().progress()).isEqualTo(40);

        ResponseEntity<GoalDto> statusRes = patch("/api/goals/" + goal.id() + "/status", token,
                new StatusUpdateRequest(GoalStatus.COMPLETED), GoalDto.class);
        assertThat(statusRes.getBody().status()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(statusRes.getBody().progress()).isEqualTo(100);

        ResponseEntity<Void> deleteRes = delete("/api/goals/" + goal.id(), token, Void.class);
        assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Object> getAfterDelete = get("/api/goals/" + goal.id(), token, Object.class);
        assertThat(getAfterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void milestonesDriveGoalProgress() {
        String token = registerAndGetToken();
        GoalDto goal = post("/api/goals", token,
                new GoalCreateRequest("Learn Spanish", null, "Education", GoalPriority.MEDIUM, null),
                GoalDto.class).getBody();

        MilestoneDto m1 = post("/api/goals/" + goal.id() + "/milestones", token,
                new MilestoneCreateRequest("Finish beginner course"), MilestoneDto.class).getBody();
        post("/api/goals/" + goal.id() + "/milestones", token,
                new MilestoneCreateRequest("Hold a 10-minute conversation"), MilestoneDto.class);

        ResponseEntity<MilestoneDto> toggled = put("/api/milestones/" + m1.id(), token,
                new MilestoneUpdateRequest(m1.title(), true), MilestoneDto.class);
        assertThat(toggled.getBody().done()).isTrue();

        ResponseEntity<GoalDto> goalAfter = get("/api/goals/" + goal.id(), token, GoalDto.class);
        assertThat(goalAfter.getBody().progress()).isEqualTo(50);
    }

    @Test
    void rejectsBlankTitle() {
        String token = registerAndGetToken();
        ResponseEntity<Object> res = post("/api/goals", token,
                new GoalCreateRequest("", null, null, GoalPriority.LOW, null), Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

package com.aakash.goalkeeper.tag;

import com.aakash.goalkeeper.AbstractIntegrationTest;
import com.aakash.goalkeeper.activity.dto.ActivityDtos.ActivityDto;
import com.aakash.goalkeeper.goal.GoalPriority;
import com.aakash.goalkeeper.goal.dto.GoalDtos.GoalCreateRequest;
import com.aakash.goalkeeper.goal.dto.GoalDtos.GoalDto;
import com.aakash.goalkeeper.goal.dto.PageResponse;
import com.aakash.goalkeeper.tag.dto.TagDtos.TagCreateRequest;
import com.aakash.goalkeeper.tag.dto.TagDtos.TagDto;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class TagActivityTest extends AbstractIntegrationTest {

    @Test
    void createsAttachesFiltersAndDetachesTags() {
        String token = registerAndGetToken();
        TagDto tag = post("/api/tags", token, new TagCreateRequest("Fitness"), TagDto.class).getBody();

        GoalDto goal = post("/api/goals", token,
                new GoalCreateRequest("Run a 10k", null, null, GoalPriority.LOW, null),
                GoalDto.class).getBody();

        ResponseEntity<Void> attach = post("/api/goals/" + goal.id() + "/tags/" + tag.id(), token, null, Void.class);
        assertThat(attach.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<TagDto[]> goalTags = get("/api/goals/" + goal.id() + "/tags", token, TagDto[].class);
        assertThat(goalTags.getBody()).extracting(TagDto::name).containsExactly("Fitness");

        ResponseEntity<PageResponse<GoalDto>> filtered = rest.exchange(
                baseUrl() + "/api/goals?tag=Fitness", HttpMethod.GET, new HttpEntity<>(authHeaders(token)),
                new ParameterizedTypeReference<PageResponse<GoalDto>>() {});
        assertThat(filtered.getBody().items()).extracting(GoalDto::id).containsExactly(goal.id());

        ResponseEntity<PageResponse<GoalDto>> noMatch = rest.exchange(
                baseUrl() + "/api/goals?tag=Nope", HttpMethod.GET, new HttpEntity<>(authHeaders(token)),
                new ParameterizedTypeReference<PageResponse<GoalDto>>() {});
        assertThat(noMatch.getBody().items()).isEmpty();

        ResponseEntity<Void> detach = delete("/api/goals/" + goal.id() + "/tags/" + tag.id(), token, Void.class);
        assertThat(detach.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(get("/api/goals/" + goal.id() + "/tags", token, TagDto[].class).getBody()).isEmpty();
    }

    @Test
    void otherUserCannotAttachOrSeeAnotherUsersTags() {
        String owner = registerAndGetToken();
        String intruder = registerAndGetToken();

        TagDto ownerTag = post("/api/tags", owner, new TagCreateRequest("Private"), TagDto.class).getBody();
        GoalDto goal = post("/api/goals", owner,
                new GoalCreateRequest("Goal", null, null, GoalPriority.LOW, null), GoalDto.class).getBody();

        assertThat(post("/api/goals/" + goal.id() + "/tags/" + ownerTag.id(), intruder, null, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<TagDto[]> intrudersTags = get("/api/tags", intruder, TagDto[].class);
        assertThat(intrudersTags.getBody()).isEmpty();
    }

    @Test
    void goalMutationsAreRecordedInActivityLog() {
        String token = registerAndGetToken();
        GoalDto goal = post("/api/goals", token,
                new GoalCreateRequest("Read more", null, null, GoalPriority.LOW, null),
                GoalDto.class).getBody();

        patch("/api/goals/" + goal.id() + "/status", token,
                new com.aakash.goalkeeper.goal.dto.GoalDtos.StatusUpdateRequest(
                        com.aakash.goalkeeper.goal.GoalStatus.COMPLETED),
                GoalDto.class);

        ResponseEntity<PageResponse<ActivityDto>> activity = rest.exchange(
                baseUrl() + "/api/goals/" + goal.id() + "/activity", HttpMethod.GET, new HttpEntity<>(authHeaders(token)),
                new ParameterizedTypeReference<PageResponse<ActivityDto>>() {});
        assertThat(activity.getBody().items()).extracting(ActivityDto::type)
                .contains(com.aakash.goalkeeper.activity.ActivityType.GOAL_CREATED,
                        com.aakash.goalkeeper.activity.ActivityType.STATUS_CHANGED);
    }
}

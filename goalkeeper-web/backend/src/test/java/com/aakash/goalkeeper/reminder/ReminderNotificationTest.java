package com.aakash.goalkeeper.reminder;

import com.aakash.goalkeeper.AbstractIntegrationTest;
import com.aakash.goalkeeper.goal.GoalPriority;
import com.aakash.goalkeeper.goal.dto.GoalDtos.GoalCreateRequest;
import com.aakash.goalkeeper.goal.dto.GoalDtos.GoalDto;
import com.aakash.goalkeeper.goal.dto.PageResponse;
import com.aakash.goalkeeper.notification.dto.NotificationDtos.NotificationDto;
import com.aakash.goalkeeper.notification.dto.NotificationDtos.UnreadCount;
import com.aakash.goalkeeper.reminder.dto.ReminderDtos.ReminderCreateRequest;
import com.aakash.goalkeeper.reminder.dto.ReminderDtos.ReminderDto;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ReminderNotificationTest extends AbstractIntegrationTest {

    @Test
    void createsListsAndDeletesReminders_enforcingTheThreeReminderCap() {
        String token = registerAndGetToken();
        GoalDto goal = post("/api/goals", token,
                new GoalCreateRequest("Read 12 books", null, null, GoalPriority.LOW, null),
                GoalDto.class).getBody();

        for (int i = 1; i <= 3; i++) {
            ResponseEntity<ReminderDto> res = post("/api/goals/" + goal.id() + "/reminders", token,
                    new ReminderCreateRequest(Instant.now().plus(i, ChronoUnit.DAYS), ReminderChannel.IN_APP),
                    ReminderDto.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        ResponseEntity<Object> fourth = post("/api/goals/" + goal.id() + "/reminders", token,
                new ReminderCreateRequest(Instant.now().plus(4, ChronoUnit.DAYS), ReminderChannel.IN_APP), Object.class);
        assertThat(fourth.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<ReminderDto[]> list = get("/api/goals/" + goal.id() + "/reminders", token, ReminderDto[].class);
        assertThat(list.getBody()).hasSize(3);

        Object firstId = list.getBody()[0].id();
        ResponseEntity<Void> deleteRes = delete("/api/goals/" + goal.id() + "/reminders/" + firstId, token, Void.class);
        assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void otherUserCannotSeeOrMutateReminders() {
        String owner = registerAndGetToken();
        String intruder = registerAndGetToken();

        GoalDto goal = post("/api/goals", owner,
                new GoalCreateRequest("Private goal", null, null, GoalPriority.LOW, null),
                GoalDto.class).getBody();
        ReminderDto reminder = post("/api/goals/" + goal.id() + "/reminders", owner,
                new ReminderCreateRequest(Instant.now().plus(1, ChronoUnit.DAYS), ReminderChannel.IN_APP),
                ReminderDto.class).getBody();

        assertThat(get("/api/goals/" + goal.id() + "/reminders", intruder, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete("/api/goals/" + goal.id() + "/reminders/" + reminder.id(), intruder, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void notificationsListMarkReadAndUnreadCountAreScopedToTheCaller() {
        String token = registerAndGetToken();

        ResponseEntity<UnreadCount> before = get("/api/notifications/unread-count", token, UnreadCount.class);
        assertThat(before.getBody().count()).isZero();

        ResponseEntity<PageResponse<NotificationDto>> list = rest.exchange(
                baseUrl() + "/api/notifications", HttpMethod.GET, new HttpEntity<>(authHeaders(token)),
                new ParameterizedTypeReference<PageResponse<NotificationDto>>() {});
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody().items()).isEmpty();

        ResponseEntity<Void> markAll = post("/api/notifications/read-all", token, null, Void.class);
        assertThat(markAll.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

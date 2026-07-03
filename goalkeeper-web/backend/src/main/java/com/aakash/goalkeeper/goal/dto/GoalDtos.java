package com.aakash.goalkeeper.goal.dto;

import com.aakash.goalkeeper.goal.GoalPriority;
import com.aakash.goalkeeper.goal.GoalStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class GoalDtos {

    public record GoalCreateRequest(
            @NotBlank @Size(max = 200) String title,
            String description,
            @Size(max = 60) String category,
            GoalPriority priority,
            Instant targetDate
    ) {}

    public record GoalUpdateRequest(
            @NotBlank @Size(max = 200) String title,
            String description,
            @Size(max = 60) String category,
            @NotNull GoalPriority priority,
            Instant targetDate
    ) {}

    public record StatusUpdateRequest(@NotNull GoalStatus status) {}

    public record ProgressUpdateRequest(@Min(0) @Max(100) int progress) {}

    public record GoalDto(
            UUID id,
            String title,
            String description,
            String category,
            GoalPriority priority,
            GoalStatus status,
            int progress,
            Instant targetDate,
            Instant createdAt,
            Instant updatedAt
    ) {}
}

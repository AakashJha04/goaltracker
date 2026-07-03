package com.aakash.goalkeeper.goal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class MilestoneDtos {

    public record MilestoneCreateRequest(@NotBlank @Size(max = 200) String title) {}

    public record MilestoneUpdateRequest(@NotBlank @Size(max = 200) String title, boolean done) {}

    public record ReorderRequest(@NotEmpty List<UUID> orderedIds) {}

    public record MilestoneDto(UUID id, String title, boolean done, int position) {}
}

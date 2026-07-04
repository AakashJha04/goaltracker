package com.aakash.goalkeeper.activity.dto;

import com.aakash.goalkeeper.activity.ActivityType;

import java.time.Instant;
import java.util.UUID;

public class ActivityDtos {
    public record ActivityDto(UUID id, ActivityType type, String description, Instant createdAt) {}
}

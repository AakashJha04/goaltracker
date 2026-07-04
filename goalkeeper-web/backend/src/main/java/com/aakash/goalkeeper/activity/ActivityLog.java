package com.aakash.goalkeeper.activity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_log")
@Getter
@Setter
public class ActivityLog {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}

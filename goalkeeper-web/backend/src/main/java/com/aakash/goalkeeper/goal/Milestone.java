package com.aakash.goalkeeper.goal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "milestones")
@Getter
@Setter
public class Milestone {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean done = false;

    @Column(nullable = false)
    private int position = 0;
}

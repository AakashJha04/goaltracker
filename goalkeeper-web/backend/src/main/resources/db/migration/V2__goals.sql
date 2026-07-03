-- Goals and milestones for Phase 2.

CREATE TABLE goals (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    category     VARCHAR(60),
    priority     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    progress     INT          NOT NULL DEFAULT 0,
    target_date  TIMESTAMPTZ,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_goals_user_status_target ON goals (user_id, status, target_date);

CREATE TABLE milestones (
    id       UUID PRIMARY KEY,
    goal_id  UUID NOT NULL REFERENCES goals (id) ON DELETE CASCADE,
    title    VARCHAR(200) NOT NULL,
    done     BOOLEAN NOT NULL DEFAULT FALSE,
    position INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_milestones_goal ON milestones (goal_id);

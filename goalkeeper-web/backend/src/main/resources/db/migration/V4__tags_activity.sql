-- Tags and per-goal activity log for Phase 4.

CREATE TABLE tags (
    id      UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name    VARCHAR(50) NOT NULL,
    UNIQUE (user_id, name)
);

CREATE TABLE goal_tags (
    goal_id UUID NOT NULL REFERENCES goals (id) ON DELETE CASCADE,
    tag_id  UUID NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (goal_id, tag_id)
);

CREATE INDEX idx_goal_tags_tag ON goal_tags (tag_id);

CREATE TABLE activity_log (
    id          UUID PRIMARY KEY,
    goal_id     UUID NOT NULL REFERENCES goals (id) ON DELETE CASCADE,
    type        VARCHAR(30) NOT NULL,
    description VARCHAR(300) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_goal_created ON activity_log (goal_id, created_at DESC);

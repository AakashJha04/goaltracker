-- Reminders and notifications for Phase 3 (in-process async first).

CREATE TABLE reminders (
    id         UUID PRIMARY KEY,
    goal_id    UUID NOT NULL REFERENCES goals (id) ON DELETE CASCADE,
    remind_at  TIMESTAMPTZ NOT NULL,
    channel    VARCHAR(20) NOT NULL DEFAULT 'IN_APP',
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_reminders_status_remind_at ON reminders (status, remind_at);

CREATE TABLE notifications (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    goal_id    UUID REFERENCES goals (id) ON DELETE CASCADE,
    type       VARCHAR(30)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    body       TEXT,
    read       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_read_created ON notifications (user_id, read, created_at DESC);

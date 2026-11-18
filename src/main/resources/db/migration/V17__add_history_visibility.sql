ALTER TABLE activity_logs ADD COLUMN history_hidden boolean NOT NULL DEFAULT false;
CREATE INDEX idx_activity_history_actor ON activity_logs(actor_id, created_at DESC) WHERE history_hidden = false;

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    type notification_type NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500),
    entity_type VARCHAR(50),
    entity_id UUID,
    action_url TEXT,
    metadata JSONB NOT NULL DEFAULT '{}'::JSONB,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notification_metadata CHECK (jsonb_typeof(metadata) = 'object'),
    CONSTRAINT chk_notification_read CHECK (
        (is_read = FALSE AND read_at IS NULL) OR is_read = TRUE
    )
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, created_at DESC);
CREATE INDEX idx_notifications_unread ON notifications(recipient_id, created_at DESC) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_entity ON notifications(entity_type, entity_id);

CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    entity_name VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    ip_hash VARCHAR(255),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_activity_old_values CHECK (
        old_values IS NULL OR jsonb_typeof(old_values) = 'object'
    ),
    CONSTRAINT chk_activity_new_values CHECK (
        new_values IS NULL OR jsonb_typeof(new_values) = 'object'
    )
);

CREATE INDEX idx_activity_logs_workspace ON activity_logs(workspace_id, created_at DESC);
CREATE INDEX idx_activity_logs_actor ON activity_logs(actor_id, created_at DESC);
CREATE INDEX idx_activity_logs_entity ON activity_logs(entity_type, entity_id);

CREATE TABLE workspace_api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    key_prefix VARCHAR(20) NOT NULL,
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    permissions JSONB NOT NULL DEFAULT '[]'::JSONB,
    last_used_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_api_key_permissions CHECK (jsonb_typeof(permissions) = 'array')
);

CREATE INDEX idx_workspace_api_keys_workspace ON workspace_api_keys(workspace_id);
CREATE INDEX idx_workspace_api_keys_prefix ON workspace_api_keys(key_prefix);

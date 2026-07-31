CREATE TABLE prompt_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    workspace_id UUID REFERENCES workspaces(id) ON DELETE SET NULL,
    prompt_id UUID REFERENCES prompts(id) ON DELETE SET NULL,
    prompt_version_id UUID REFERENCES prompt_versions(id) ON DELETE SET NULL,
    input_values JSONB NOT NULL DEFAULT '{}'::JSONB,
    rendered_prompt TEXT NOT NULL,
    response_content TEXT,
    provider VARCHAR(50),
    model VARCHAR(100),
    status run_status NOT NULL DEFAULT 'PENDING',
    input_tokens INTEGER,
    output_tokens INTEGER,
    total_tokens INTEGER,
    estimated_cost NUMERIC(14, 8),
    execution_time_ms INTEGER,
    error_code VARCHAR(100),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT chk_prompt_run_input_values CHECK (jsonb_typeof(input_values) = 'object'),
    CONSTRAINT chk_prompt_run_tokens CHECK (
        (input_tokens IS NULL OR input_tokens >= 0)
        AND (output_tokens IS NULL OR output_tokens >= 0)
        AND (total_tokens IS NULL OR total_tokens >= 0)
    ),
    CONSTRAINT chk_prompt_run_cost CHECK (estimated_cost IS NULL OR estimated_cost >= 0),
    CONSTRAINT chk_execution_time CHECK (execution_time_ms IS NULL OR execution_time_ms >= 0)
);

CREATE INDEX idx_prompt_runs_user_created ON prompt_runs(user_id, created_at DESC);
CREATE INDEX idx_prompt_runs_prompt_created ON prompt_runs(prompt_id, created_at DESC);
CREATE INDEX idx_prompt_runs_workspace ON prompt_runs(workspace_id, created_at DESC);
CREATE INDEX idx_prompt_runs_status ON prompt_runs(status);
CREATE INDEX idx_prompt_runs_model ON prompt_runs(provider, model);

CREATE TABLE prompt_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prompt_id UUID NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating SMALLINT NOT NULL,
    content VARCHAR(2000),
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_prompt_review_user UNIQUE (prompt_id, user_id),
    CONSTRAINT chk_prompt_review_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_prompt_reviews_prompt_created ON prompt_reviews(prompt_id, created_at DESC);
CREATE INDEX idx_prompt_reviews_user ON prompt_reviews(user_id);

CREATE TABLE prompt_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prompt_id UUID NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES prompt_comments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_comment_content CHECK (LENGTH(TRIM(content)) > 0)
);

CREATE INDEX idx_prompt_comments_prompt ON prompt_comments(prompt_id, created_at DESC);
CREATE INDEX idx_prompt_comments_parent ON prompt_comments(parent_id);
CREATE INDEX idx_prompt_comments_user ON prompt_comments(user_id);

CREATE TABLE prompt_views (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prompt_id UUID NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(255),
    ip_hash VARCHAR(255),
    viewed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prompt_views_prompt ON prompt_views(prompt_id, viewed_at DESC);
CREATE INDEX idx_prompt_views_user ON prompt_views(user_id, viewed_at DESC);
CREATE INDEX idx_prompt_views_session ON prompt_views(prompt_id, session_id, viewed_at DESC);

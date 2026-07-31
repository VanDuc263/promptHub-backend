CREATE TABLE prompts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL,
    short_description VARCHAR(300),
    description TEXT,
    visibility prompt_visibility NOT NULL DEFAULT 'PRIVATE',
    status prompt_status NOT NULL DEFAULT 'DRAFT',
    current_version_id UUID,
    cover_image_url TEXT,
    icon VARCHAR(100),
    model_provider VARCHAR(50),
    recommended_model VARCHAR(100),
    language VARCHAR(20) DEFAULT 'en',
    forked_from_prompt_id UUID REFERENCES prompts(id) ON DELETE SET NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    run_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    fork_count BIGINT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    archived_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_prompt_workspace_slug UNIQUE (workspace_id, slug),
    CONSTRAINT chk_prompt_counts CHECK (
        view_count >= 0 AND run_count >= 0 AND favorite_count >= 0 AND fork_count >= 0
    )
);

CREATE INDEX idx_prompts_workspace ON prompts(workspace_id);
CREATE INDEX idx_prompts_author ON prompts(author_id);
CREATE INDEX idx_prompts_visibility_status ON prompts(visibility, status);
CREATE INDEX idx_prompts_created_at ON prompts(created_at DESC);
CREATE INDEX idx_prompts_updated_at ON prompts(updated_at DESC);
CREATE INDEX idx_prompts_popular ON prompts(favorite_count DESC, run_count DESC);
CREATE INDEX idx_prompts_forked_from ON prompts(forked_from_prompt_id);

CREATE TABLE prompt_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prompt_id UUID NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    content TEXT NOT NULL,
    system_message TEXT,
    variables JSONB NOT NULL DEFAULT '[]'::JSONB,
    model_config JSONB NOT NULL DEFAULT '{}'::JSONB,
    changelog VARCHAR(500),
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_prompt_version UNIQUE (prompt_id, version_number),
    CONSTRAINT chk_version_number CHECK (version_number > 0),
    CONSTRAINT chk_variables_array CHECK (jsonb_typeof(variables) = 'array'),
    CONSTRAINT chk_model_config_object CHECK (jsonb_typeof(model_config) = 'object')
);

CREATE INDEX idx_prompt_versions_prompt ON prompt_versions(prompt_id, version_number DESC);
CREATE INDEX idx_prompt_versions_creator ON prompt_versions(created_by);

ALTER TABLE prompts
ADD CONSTRAINT fk_prompts_current_version
FOREIGN KEY (current_version_id)
REFERENCES prompt_versions(id)
ON DELETE SET NULL;

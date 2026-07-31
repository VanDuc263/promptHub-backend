CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(60) NOT NULL UNIQUE,
    description VARCHAR(255),
    usage_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tag_usage_count CHECK (usage_count >= 0)
);

CREATE UNIQUE INDEX uq_tags_name_lower ON tags(LOWER(name));

CREATE TABLE prompt_tags (
    prompt_id UUID NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (prompt_id, tag_id)
);

CREATE INDEX idx_prompt_tags_tag_id ON prompt_tags(tag_id);

CREATE TABLE favorites (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    prompt_id UUID NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, prompt_id)
);

CREATE INDEX idx_favorites_user_created ON favorites(user_id, created_at DESC);
CREATE INDEX idx_favorites_prompt ON favorites(prompt_id);

CREATE TABLE collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(170) NOT NULL,
    description VARCHAR(500),
    cover_image_url TEXT,
    visibility collection_visibility NOT NULL DEFAULT 'PRIVATE',
    prompt_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_collection_prompt_count CHECK (prompt_count >= 0)
);

CREATE UNIQUE INDEX uq_user_collection_slug
    ON collections(owner_id, slug)
    WHERE workspace_id IS NULL;

CREATE UNIQUE INDEX uq_workspace_collection_slug
    ON collections(workspace_id, slug)
    WHERE workspace_id IS NOT NULL;

CREATE INDEX idx_collections_owner ON collections(owner_id);
CREATE INDEX idx_collections_workspace ON collections(workspace_id);
CREATE INDEX idx_collections_visibility ON collections(visibility);

CREATE TABLE collection_prompts (
    collection_id UUID NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    prompt_id UUID NOT NULL REFERENCES prompts(id) ON DELETE CASCADE,
    added_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    position INTEGER NOT NULL DEFAULT 0,
    note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (collection_id, prompt_id),
    CONSTRAINT chk_collection_prompt_position CHECK (position >= 0)
);

CREATE INDEX idx_collection_prompts_order ON collection_prompts(collection_id, position, created_at);
CREATE INDEX idx_collection_prompts_prompt ON collection_prompts(prompt_id);

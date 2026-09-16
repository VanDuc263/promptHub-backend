ALTER TABLE prompts
    ADD COLUMN prompt_type VARCHAR(20) NOT NULL DEFAULT 'text',
    ADD COLUMN input_preview_url TEXT,
    ADD COLUMN output_preview_url TEXT,
    ADD CONSTRAINT chk_prompts_type CHECK (prompt_type IN ('text', 'image'));

CREATE INDEX idx_prompts_type ON prompts(prompt_type);

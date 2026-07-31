ALTER TABLE prompts ADD COLUMN search_vector TSVECTOR;

CREATE OR REPLACE FUNCTION update_prompt_search_vector()
RETURNS TRIGGER AS $$
DECLARE
    latest_content TEXT;
BEGIN
    SELECT pv.content
    INTO latest_content
    FROM prompt_versions pv
    WHERE pv.id = NEW.current_version_id;

    NEW.search_vector :=
        SETWEIGHT(TO_TSVECTOR('simple', COALESCE(NEW.title, '')), 'A') ||
        SETWEIGHT(TO_TSVECTOR('simple', COALESCE(NEW.short_description, '')), 'B') ||
        SETWEIGHT(TO_TSVECTOR('simple', COALESCE(NEW.description, '')), 'C') ||
        SETWEIGHT(TO_TSVECTOR('simple', COALESCE(latest_content, '')), 'D');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_prompt_search_vector
BEFORE INSERT OR UPDATE OF title, short_description, description, current_version_id
ON prompts
FOR EACH ROW
EXECUTE FUNCTION update_prompt_search_vector();

CREATE INDEX idx_prompts_search_vector ON prompts USING GIN(search_vector);

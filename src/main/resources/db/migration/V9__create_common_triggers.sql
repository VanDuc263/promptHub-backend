CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_user_oauth_accounts_updated_at
BEFORE UPDATE ON user_oauth_accounts
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_workspaces_updated_at
BEFORE UPDATE ON workspaces
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_workspace_members_updated_at
BEFORE UPDATE ON workspace_members
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_prompts_updated_at
BEFORE UPDATE ON prompts
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_collections_updated_at
BEFORE UPDATE ON collections
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_prompt_reviews_updated_at
BEFORE UPDATE ON prompt_reviews
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_prompt_comments_updated_at
BEFORE UPDATE ON prompt_comments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE OR REPLACE FUNCTION update_prompt_favorite_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE prompts
        SET favorite_count = favorite_count + 1
        WHERE id = NEW.prompt_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE prompts
        SET favorite_count = GREATEST(favorite_count - 1, 0)
        WHERE id = OLD.prompt_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prompt_favorite_count
AFTER INSERT OR DELETE ON favorites
FOR EACH ROW EXECUTE FUNCTION update_prompt_favorite_count();

CREATE OR REPLACE FUNCTION update_collection_prompt_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE collections
        SET prompt_count = prompt_count + 1
        WHERE id = NEW.collection_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE collections
        SET prompt_count = GREATEST(prompt_count - 1, 0)
        WHERE id = OLD.collection_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_collection_prompt_count
AFTER INSERT OR DELETE ON collection_prompts
FOR EACH ROW EXECUTE FUNCTION update_collection_prompt_count();

CREATE OR REPLACE FUNCTION update_prompt_run_count()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'SUCCESS'
       AND OLD.status IS DISTINCT FROM 'SUCCESS'
       AND NEW.prompt_id IS NOT NULL THEN
        UPDATE prompts
        SET run_count = run_count + 1
        WHERE id = NEW.prompt_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prompt_run_count
AFTER UPDATE OF status ON prompt_runs
FOR EACH ROW EXECUTE FUNCTION update_prompt_run_count();

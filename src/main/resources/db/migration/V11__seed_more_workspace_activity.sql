-- Additional development activity covering the workspace timeline filters and entity types.
INSERT INTO activity_logs (
    id, workspace_id, actor_id, action, entity_type, entity_id,
    entity_name, old_values, new_values, ip_hash, user_agent, created_at
) VALUES
(
    '71000000-0000-0000-0000-000000000004',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000003',
    'PROMPT_UPDATED', 'PROMPT', '30000000-0000-0000-0000-000000000002',
    'SEO Blog Outline Generator', '{"version":"1.0"}'::jsonb, '{"version":"1.1"}'::jsonb,
    encode(digest('127.0.0.3', 'sha256'), 'hex'), 'PromptHub Seed Client', NOW() - INTERVAL '2 hours'
),
(
    '71000000-0000-0000-0000-000000000005',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'COLLECTION_CREATED', 'COLLECTION', '50000000-0000-0000-0000-000000000002',
    'Design & Content Toolkit', NULL, '{"visibility":"WORKSPACE"}'::jsonb,
    encode(digest('127.0.0.2', 'sha256'), 'hex'), 'PromptHub Seed Client', NOW() - INTERVAL '1 day'
),
(
    '71000000-0000-0000-0000-000000000006',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    'MEMBER_JOINED', 'WORKSPACE_MEMBER', '21000000-0000-0000-0000-000000000005',
    'An Phạm', NULL, '{"role":"VIEWER"}'::jsonb,
    encode(digest('127.0.0.1', 'sha256'), 'hex'), 'PromptHub Seed Client', NOW() - INTERVAL '3 days'
),
(
    '71000000-0000-0000-0000-000000000007',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'COMMENT_CREATED', 'PROMPT_COMMENT', NULL,
    'Spring Boot Code Reviewer', NULL, '{"comment":"Added review notes"}'::jsonb,
    encode(digest('127.0.0.2', 'sha256'), 'hex'), 'PromptHub Seed Client', NOW() - INTERVAL '10 days'
)
ON CONFLICT (id) DO NOTHING;

-- A public image prompt with before/after examples for the Explore marketplace.
INSERT INTO prompts (
    id, workspace_id, author_id, title, slug, short_description, description,
    visibility, status, prompt_type, input_preview_url, output_preview_url,
    icon, model_provider, recommended_model, language, view_count, run_count,
    favorite_count, fork_count, is_featured, published_at, created_at, updated_at
) VALUES (
    '30000000-0000-0000-0000-000000000006',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'Cinematic Landscape Restyle', 'cinematic-landscape-restyle',
    'Transform a reference landscape into a cinematic, art-directed scene while preserving its composition.',
    'Image-to-image prompt with a reference image and a finished visual example so creators can judge the intended transformation before copying.',
    'PUBLIC', 'PUBLISHED', 'image',
    'https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=85',
    'https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?auto=format&fit=crop&w=900&q=85',
    'image', 'GOOGLE', 'gemini-2.5-flash-image', 'en', 1560, 0, 0, 3, TRUE,
    NOW() - INTERVAL '2 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'
);

INSERT INTO prompt_versions (
    id, prompt_id, version_number, content, system_message, variables,
    model_config, changelog, created_by, created_at
) VALUES (
    '31000000-0000-0000-0000-000000000007',
    '30000000-0000-0000-0000-000000000006', 1,
    'Use the reference image as the composition guide. Transform {{subject}} into a {{style}} scene while keeping the main horizon, camera angle, and subject placement. Use {{lighting}} lighting, cinematic detail, and a {{aspect_ratio}} frame.',
    'You are an image art director. Preserve the defining layout of the input image and make the requested visual transformation clear and coherent.',
    '[{"name":"subject","label":"Subject","type":"text","required":true,"placeholder":"e.g. mountain valley"},{"name":"style","label":"Visual style","type":"text","required":true,"placeholder":"e.g. surreal nightscape"},{"name":"lighting","label":"Lighting","type":"text","required":true,"placeholder":"e.g. moonlit blue hour"},{"name":"aspect_ratio","label":"Aspect ratio","type":"text","required":false,"placeholder":"e.g. 16:9"}]'::jsonb,
    '{"model":"gemini-2.5-flash-image","maxTokens":900}'::jsonb,
    'Added input and output preview examples.',
    '10000000-0000-0000-0000-000000000002', NOW() - INTERVAL '3 days'
);

UPDATE prompts
SET current_version_id = '31000000-0000-0000-0000-000000000007'
WHERE id = '30000000-0000-0000-0000-000000000006';

INSERT INTO tags (id, name, slug, description, usage_count, created_at) VALUES
    ('40000000-0000-0000-0000-000000000007', 'Image', 'image', 'Image generation and image-to-image prompts.', 1, NOW() - INTERVAL '3 days'),
    ('40000000-0000-0000-0000-000000000008', 'Cinematic', 'cinematic', 'Prompts for cinematic visual direction.', 1, NOW() - INTERVAL '3 days'),
    ('40000000-0000-0000-0000-000000000009', 'Image to Image', 'image-to-image', 'Reference-driven image transformations.', 1, NOW() - INTERVAL '3 days');

INSERT INTO prompt_tags (prompt_id, tag_id, created_at) VALUES
    ('30000000-0000-0000-0000-000000000006', '40000000-0000-0000-0000-000000000007', NOW() - INTERVAL '3 days'),
    ('30000000-0000-0000-0000-000000000006', '40000000-0000-0000-0000-000000000008', NOW() - INTERVAL '3 days'),
    ('30000000-0000-0000-0000-000000000006', '40000000-0000-0000-0000-000000000009', NOW() - INTERVAL '3 days');

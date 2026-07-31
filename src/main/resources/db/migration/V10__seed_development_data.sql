-- Development seed data for PromptHub.
-- Do not run this migration in production.

-- Password for all demo accounts: Password@123
INSERT INTO users (
    id, username, email, password_hash, display_name, avatar_url, bio,
    website_url, location, status, email_verified, is_system_admin,
    created_at, updated_at, last_login_at
) VALUES
(
    '10000000-0000-0000-0000-000000000001',
    'ducngo', 'duc@example.com', crypt('Password@123', gen_salt('bf')),
    'Đức Ngô', 'https://api.dicebear.com/9.x/initials/svg?seed=Duc%20Ngo',
    'Java Backend Developer, yêu thích Spring Boot và PostgreSQL.',
    'https://github.com/ducngo', 'Ho Chi Minh City, Vietnam',
    'ACTIVE', TRUE, TRUE,
    NOW() - INTERVAL '90 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '2 hours'
),
(
    '10000000-0000-0000-0000-000000000002',
    'linhnguyen', 'linh@example.com', crypt('Password@123', gen_salt('bf')),
    'Linh Nguyễn', 'https://api.dicebear.com/9.x/initials/svg?seed=Linh%20Nguyen',
    'Product designer và prompt creator.',
    NULL, 'Da Nang, Vietnam',
    'ACTIVE', TRUE, FALSE,
    NOW() - INTERVAL '75 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day'
),
(
    '10000000-0000-0000-0000-000000000003',
    'minhtran', 'minh@example.com', crypt('Password@123', gen_salt('bf')),
    'Minh Trần', 'https://api.dicebear.com/9.x/initials/svg?seed=Minh%20Tran',
    'Content creator chuyên về AI và marketing.',
    NULL, 'Ha Noi, Vietnam',
    'ACTIVE', TRUE, FALSE,
    NOW() - INTERVAL '60 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '3 days'
),
(
    '10000000-0000-0000-0000-000000000004',
    'anpham', 'an@example.com', crypt('Password@123', gen_salt('bf')),
    'An Phạm', 'https://api.dicebear.com/9.x/initials/svg?seed=An%20Pham',
    'Sinh viên và người dùng PromptHub.',
    NULL, 'Can Tho, Vietnam',
    'ACTIVE', TRUE, FALSE,
    NOW() - INTERVAL '30 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '5 days'
);

INSERT INTO user_oauth_accounts (
    id, user_id, provider, provider_user_id, provider_email, created_at, updated_at
) VALUES
(
    '11000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    'GITHUB', 'github-ducngo-demo', 'duc@example.com',
    NOW() - INTERVAL '80 days', NOW() - INTERVAL '1 day'
),
(
    '11000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'GOOGLE', 'google-linhnguyen-demo', 'linh@example.com',
    NOW() - INTERVAL '70 days', NOW() - INTERVAL '3 days'
);

INSERT INTO workspaces (
    id, name, slug, description, avatar_url, owner_id, is_personal,
    created_at, updated_at
) VALUES
(
    '20000000-0000-0000-0000-000000000001',
    'Đức Personal', 'duc-personal', 'Không gian cá nhân của Đức.', NULL,
    '10000000-0000-0000-0000-000000000001', TRUE,
    NOW() - INTERVAL '90 days', NOW() - INTERVAL '1 day'
),
(
    '20000000-0000-0000-0000-000000000002',
    'PromptHub Team', 'prompthub-team',
    'Workspace xây dựng và quản lý các prompt dùng chung cho đội ngũ.',
    'https://api.dicebear.com/9.x/shapes/svg?seed=PromptHub',
    '10000000-0000-0000-0000-000000000001', FALSE,
    NOW() - INTERVAL '65 days', NOW() - INTERVAL '2 hours'
),
(
    '20000000-0000-0000-0000-000000000003',
    'Linh Personal', 'linh-personal', 'Không gian cá nhân của Linh.', NULL,
    '10000000-0000-0000-0000-000000000002', TRUE,
    NOW() - INTERVAL '75 days', NOW() - INTERVAL '3 days'
);

INSERT INTO workspace_members (
    id, workspace_id, user_id, role, joined_at, updated_at
) VALUES
('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'OWNER', NOW() - INTERVAL '90 days', NOW() - INTERVAL '1 day'),
('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'OWNER', NOW() - INTERVAL '65 days', NOW() - INTERVAL '2 hours'),
('21000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'ADMIN', NOW() - INTERVAL '60 days', NOW() - INTERVAL '3 days'),
('21000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 'EDITOR', NOW() - INTERVAL '45 days', NOW() - INTERVAL '5 days'),
('21000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004', 'VIEWER', NOW() - INTERVAL '20 days', NOW() - INTERVAL '7 days'),
('21000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'OWNER', NOW() - INTERVAL '75 days', NOW() - INTERVAL '3 days');

INSERT INTO workspace_invitations (
    id, workspace_id, inviter_id, invitee_email, invitee_user_id, role,
    token, status, expires_at, created_at
) VALUES
(
    '22000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    'newmember@example.com', NULL, 'EDITOR',
    'demo-invitation-token-prompthub-team', 'PENDING',
    NOW() + INTERVAL '7 days', NOW() - INTERVAL '1 day'
);

INSERT INTO prompts (
    id, workspace_id, author_id, title, slug, short_description, description,
    visibility, status, cover_image_url, icon, model_provider,
    recommended_model, language, view_count, run_count, favorite_count,
    fork_count, is_featured, published_at, created_at, updated_at
) VALUES
(
    '30000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    'Spring Boot Code Reviewer', 'spring-boot-code-reviewer',
    'Review mã nguồn Spring Boot và đề xuất cách cải thiện.',
    'Phân tích controller, service, repository và các vấn đề bảo mật hoặc hiệu năng.',
    'PUBLIC', 'PUBLISHED', NULL, 'code-2', 'OPENAI', 'gpt-5', 'vi',
    1280, 0, 0, 2, TRUE,
    NOW() - INTERVAL '40 days', NOW() - INTERVAL '45 days', NOW() - INTERVAL '1 day'
),
(
    '30000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'UI Design Critic', 'ui-design-critic',
    'Đánh giá giao diện và đưa ra đề xuất UX/UI có thể thực hiện.',
    'Prompt dành cho designer cần phân tích hierarchy, spacing, typography và accessibility.',
    'PUBLIC', 'PUBLISHED', NULL, 'palette', 'ANTHROPIC', 'claude-sonnet', 'vi',
    940, 0, 0, 1, TRUE,
    NOW() - INTERVAL '32 days', NOW() - INTERVAL '35 days', NOW() - INTERVAL '3 days'
),
(
    '30000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000003',
    'SEO Blog Generator', 'seo-blog-generator',
    'Tạo dàn ý và bài viết chuẩn SEO theo từ khóa.',
    'Hỗ trợ nghiên cứu ý định tìm kiếm, cấu trúc heading và meta description.',
    'PUBLIC', 'PUBLISHED', NULL, 'file-text', 'GOOGLE', 'gemini-pro', 'vi',
    720, 0, 0, 0, FALSE,
    NOW() - INTERVAL '20 days', NOW() - INTERVAL '24 days', NOW() - INTERVAL '5 days'
),
(
    '30000000-0000-0000-0000-000000000004',
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    'Database Schema Assistant', 'database-schema-assistant',
    'Thiết kế database PostgreSQL từ yêu cầu nghiệp vụ.',
    'Prompt cá nhân dùng để phân tích entity, relationship, constraint và index.',
    'PRIVATE', 'DRAFT', NULL, 'database', 'OPENAI', 'gpt-5', 'vi',
    12, 0, 0, 0, FALSE,
    NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '2 days'
),
(
    '30000000-0000-0000-0000-000000000005',
    '20000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000002',
    'Professional Email Writer', 'professional-email-writer',
    'Viết email chuyên nghiệp theo mục tiêu và đối tượng nhận.',
    'Tạo email rõ ràng, lịch sự và có lời kêu gọi hành động phù hợp.',
    'UNLISTED', 'PUBLISHED', NULL, 'mail', 'OPENAI', 'gpt-5-mini', 'en',
    210, 0, 0, 0, FALSE,
    NOW() - INTERVAL '12 days', NOW() - INTERVAL '15 days', NOW() - INTERVAL '4 days'
);

INSERT INTO prompt_versions (
    id, prompt_id, version_number, content, system_message, variables,
    model_config, changelog, created_by, created_at
) VALUES
(
    '31000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000001', 1,
    'Hãy review đoạn code Spring Boot sau:\n\n{{code}}\n\nLiệt kê lỗi và đề xuất cải thiện.',
    'Bạn là senior Java backend developer có kinh nghiệm Spring Boot.',
    '[{"name":"code","label":"Source code","type":"textarea","required":true,"placeholder":"Paste Java code here"}]'::jsonb,
    '{"temperature":0.2,"maxTokens":1800,"topP":1}'::jsonb,
    'Phiên bản đầu tiên.',
    '10000000-0000-0000-0000-000000000001', NOW() - INTERVAL '45 days'
),
(
    '31000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000001', 2,
    'Review mã Spring Boot bên dưới:\n\n{{code}}\n\nTập trung vào: {{focus}}. Trả lời theo các mục: vấn đề, mức độ, lý do và cách sửa kèm ví dụ.',
    'Bạn là senior Java backend developer. Không suy đoán khi thiếu ngữ cảnh và ưu tiên giải pháp production-ready.',
    '[{"name":"code","label":"Source code","type":"textarea","required":true},{"name":"focus","label":"Review focus","type":"select","required":false,"defaultValue":"All","options":["All","Security","Performance","Clean Code","Database"]}]'::jsonb,
    '{"temperature":0.15,"maxTokens":2200,"topP":1}'::jsonb,
    'Thêm lựa chọn phạm vi review và cấu trúc đầu ra.',
    '10000000-0000-0000-0000-000000000001', NOW() - INTERVAL '8 days'
),
(
    '31000000-0000-0000-0000-000000000003',
    '30000000-0000-0000-0000-000000000002', 1,
    'Đánh giá giao diện {{screen}} cho nhóm người dùng {{audience}}. Phân tích visual hierarchy, spacing, typography, accessibility và đề xuất 5 thay đổi ưu tiên.',
    'Bạn là product designer giàu kinh nghiệm, đưa ra nhận xét cụ thể và có thể triển khai.',
    '[{"name":"screen","label":"Screen description","type":"textarea","required":true},{"name":"audience","label":"Target audience","type":"text","required":true}]'::jsonb,
    '{"temperature":0.5,"maxTokens":1600}'::jsonb,
    'Initial public version.',
    '10000000-0000-0000-0000-000000000002', NOW() - INTERVAL '35 days'
),
(
    '31000000-0000-0000-0000-000000000004',
    '30000000-0000-0000-0000-000000000003', 1,
    'Tạo bài blog chuẩn SEO cho từ khóa {{keyword}}, đối tượng {{audience}}, độ dài khoảng {{word_count}} từ. Bao gồm title, meta description, outline và nội dung.',
    'Bạn là SEO content strategist. Nội dung tự nhiên, tránh nhồi nhét từ khóa.',
    '[{"name":"keyword","label":"Keyword","type":"text","required":true},{"name":"audience","label":"Audience","type":"text","required":true},{"name":"word_count","label":"Word count","type":"number","required":false,"defaultValue":1200}]'::jsonb,
    '{"temperature":0.65,"maxTokens":3000}'::jsonb,
    'Initial version.',
    '10000000-0000-0000-0000-000000000003', NOW() - INTERVAL '24 days'
),
(
    '31000000-0000-0000-0000-000000000005',
    '30000000-0000-0000-0000-000000000004', 1,
    'Từ yêu cầu sau, hãy thiết kế schema PostgreSQL: {{requirements}}. Trình bày entity, relationship, DDL, index và các quyết định thiết kế.',
    'Bạn là database architect chuyên PostgreSQL.',
    '[{"name":"requirements","label":"Business requirements","type":"textarea","required":true}]'::jsonb,
    '{"temperature":0.25,"maxTokens":2500}'::jsonb,
    'Bản nháp đầu tiên.',
    '10000000-0000-0000-0000-000000000001', NOW() - INTERVAL '10 days'
),
(
    '31000000-0000-0000-0000-000000000006',
    '30000000-0000-0000-0000-000000000005', 1,
    'Write a professional email about {{topic}} to {{recipient_role}}. The desired outcome is {{outcome}}. Keep the tone {{tone}}.',
    'You write concise, professional business emails.',
    '[{"name":"topic","label":"Topic","type":"textarea","required":true},{"name":"recipient_role","label":"Recipient role","type":"text","required":true},{"name":"outcome","label":"Desired outcome","type":"text","required":true},{"name":"tone","label":"Tone","type":"select","required":false,"defaultValue":"Professional","options":["Professional","Friendly","Formal"]}]'::jsonb,
    '{"temperature":0.45,"maxTokens":900}'::jsonb,
    'Initial version.',
    '10000000-0000-0000-0000-000000000002', NOW() - INTERVAL '15 days'
);

UPDATE prompts SET current_version_id = '31000000-0000-0000-0000-000000000002' WHERE id = '30000000-0000-0000-0000-000000000001';
UPDATE prompts SET current_version_id = '31000000-0000-0000-0000-000000000003' WHERE id = '30000000-0000-0000-0000-000000000002';
UPDATE prompts SET current_version_id = '31000000-0000-0000-0000-000000000004' WHERE id = '30000000-0000-0000-0000-000000000003';
UPDATE prompts SET current_version_id = '31000000-0000-0000-0000-000000000005' WHERE id = '30000000-0000-0000-0000-000000000004';
UPDATE prompts SET current_version_id = '31000000-0000-0000-0000-000000000006' WHERE id = '30000000-0000-0000-0000-000000000005';

INSERT INTO tags (id, name, slug, description, usage_count, created_at) VALUES
('40000000-0000-0000-0000-000000000001', 'Development', 'development', 'Prompts hỗ trợ lập trình và phát triển phần mềm.', 2, NOW() - INTERVAL '50 days'),
('40000000-0000-0000-0000-000000000002', 'Java', 'java', 'Java, Spring và hệ sinh thái JVM.', 1, NOW() - INTERVAL '50 days'),
('40000000-0000-0000-0000-000000000003', 'Design', 'design', 'UI, UX và product design.', 1, NOW() - INTERVAL '40 days'),
('40000000-0000-0000-0000-000000000004', 'Marketing', 'marketing', 'Content marketing và SEO.', 1, NOW() - INTERVAL '30 days'),
('40000000-0000-0000-0000-000000000005', 'Writing', 'writing', 'Viết và chỉnh sửa nội dung.', 2, NOW() - INTERVAL '30 days'),
('40000000-0000-0000-0000-000000000006', 'Productivity', 'productivity', 'Tăng hiệu suất công việc.', 1, NOW() - INTERVAL '25 days');

INSERT INTO prompt_tags (prompt_id, tag_id, created_at) VALUES
('30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', NOW() - INTERVAL '40 days'),
('30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', NOW() - INTERVAL '40 days'),
('30000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000003', NOW() - INTERVAL '32 days'),
('30000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000004', NOW() - INTERVAL '20 days'),
('30000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000005', NOW() - INTERVAL '20 days'),
('30000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000001', NOW() - INTERVAL '10 days'),
('30000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000005', NOW() - INTERVAL '12 days'),
('30000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000006', NOW() - INTERVAL '12 days');

-- favorite_count is updated automatically by V9 trigger.
INSERT INTO favorites (user_id, prompt_id, created_at) VALUES
('10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', NOW() - INTERVAL '7 days'),
('10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', NOW() - INTERVAL '6 days'),
('10000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001', NOW() - INTERVAL '2 days'),
('10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', NOW() - INTERVAL '8 days'),
('10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', NOW() - INTERVAL '4 days'),
('10000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000003', NOW() - INTERVAL '1 day');

INSERT INTO collections (
    id, owner_id, workspace_id, name, slug, description, visibility,
    created_at, updated_at
) VALUES
(
    '50000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001', NULL,
    'Backend Essentials', 'backend-essentials',
    'Các prompt hữu ích cho công việc Java Backend.', 'PUBLIC',
    NOW() - INTERVAL '20 days', NOW() - INTERVAL '2 days'
),
(
    '50000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000002',
    'Design & Content Toolkit', 'design-content-toolkit',
    'Bộ prompt dùng chung cho thiết kế và nội dung.', 'WORKSPACE',
    NOW() - INTERVAL '18 days', NOW() - INTERVAL '3 days'
),
(
    '50000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000004', NULL,
    'Saved for Later', 'saved-for-later',
    'Danh sách prompt muốn thử sau.', 'PRIVATE',
    NOW() - INTERVAL '7 days', NOW() - INTERVAL '1 day'
);

-- prompt_count is updated automatically by V9 trigger.
INSERT INTO collection_prompts (
    collection_id, prompt_id, added_by, position, note, created_at
) VALUES
('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 0, 'Dùng để review code trước khi tạo pull request.', NOW() - INTERVAL '19 days'),
('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', 1, 'Hỗ trợ thiết kế database cho dự án mới.', NOW() - INTERVAL '9 days'),
('50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 0, NULL, NOW() - INTERVAL '17 days'),
('50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 1, NULL, NOW() - INTERVAL '15 days'),
('50000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000004', 0, NULL, NOW() - INTERVAL '5 days'),
('50000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004', 1, NULL, NOW() - INTERVAL '1 day');

-- Insert runs as PENDING, then update successful rows so V9 increments run_count.
INSERT INTO prompt_runs (
    id, user_id, workspace_id, prompt_id, prompt_version_id, input_values,
    rendered_prompt, response_content, provider, model, status,
    input_tokens, output_tokens, total_tokens, estimated_cost,
    execution_time_ms, created_at, completed_at
) VALUES
(
    '60000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000001', '31000000-0000-0000-0000-000000000002',
    '{"code":"@RestController class UserController {...}","focus":"Clean Code"}'::jsonb,
    'Review mã Spring Boot bên dưới: @RestController class UserController {...}. Tập trung vào: Clean Code.',
    'Controller đang phụ thuộc trực tiếp vào repository. Nên thêm service layer và DTO validation.',
    'OPENAI', 'gpt-5', 'PENDING', 420, 650, 1070, 0.01420000, 2450,
    NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '3 seconds'
),
(
    '60000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000002', '31000000-0000-0000-0000-000000000003',
    '{"screen":"Prompt detail page","audience":"Developers"}'::jsonb,
    'Đánh giá giao diện Prompt detail page cho nhóm người dùng Developers.',
    'Nên tăng tương phản cho nút Run, gom metadata và làm nổi bật phần prompt content.',
    'ANTHROPIC', 'claude-sonnet', 'PENDING', 280, 510, 790, 0.00810000, 1900,
    NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '2 seconds'
),
(
    '60000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002',
    '30000000-0000-0000-0000-000000000003', '31000000-0000-0000-0000-000000000004',
    '{"keyword":"Java backend roadmap","audience":"Students","word_count":1200}'::jsonb,
    'Tạo bài blog chuẩn SEO cho từ khóa Java backend roadmap, đối tượng Students.',
    'Title: Java Backend Roadmap 2026. Meta description và dàn ý chi tiết...',
    'GOOGLE', 'gemini-pro', 'PENDING', 350, 1400, 1750, 0.00650000, 4100,
    NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '5 seconds'
),
(
    '60000000-0000-0000-0000-000000000004',
    '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000004', '31000000-0000-0000-0000-000000000005',
    '{"requirements":"E-commerce database with orders and payments"}'::jsonb,
    'Từ yêu cầu sau, hãy thiết kế schema PostgreSQL: E-commerce database with orders and payments.',
    NULL, 'OPENAI', 'gpt-5', 'FAILED', 210, 0, 210, 0.00120000, 700,
    NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours' + INTERVAL '1 second'
);

UPDATE prompt_runs
SET status = 'SUCCESS'
WHERE id IN (
    '60000000-0000-0000-0000-000000000001',
    '60000000-0000-0000-0000-000000000002',
    '60000000-0000-0000-0000-000000000003'
);

INSERT INTO prompt_reviews (
    id, prompt_id, user_id, rating, content, created_at, updated_at
) VALUES
('61000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 5, 'Review rõ ràng và có ví dụ sửa code thực tế.', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
('61000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 4, 'Hữu ích, nên bổ sung thêm kiểm tra database query.', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
('61000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 5, 'Các gợi ý UX cụ thể và dễ áp dụng.', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

INSERT INTO prompt_comments (
    id, prompt_id, user_id, parent_id, content, created_at, updated_at
) VALUES
('62000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', NULL, 'Prompt có hỗ trợ review file test không?', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
('62000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '62000000-0000-0000-0000-000000000001', 'Có, bạn chọn focus là All hoặc Clean Code và gửi cả test class.', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
('62000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004', NULL, 'Nên thêm biến để chọn nền tảng web hoặc mobile.', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

INSERT INTO prompt_views (id, prompt_id, user_id, session_id, ip_hash, viewed_at) VALUES
('63000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'demo-session-001', encode(digest('127.0.0.1', 'sha256'), 'hex'), NOW() - INTERVAL '7 days'),
('63000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 'demo-session-002', encode(digest('127.0.0.2', 'sha256'), 'hex'), NOW() - INTERVAL '6 days'),
('63000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'demo-session-003', encode(digest('127.0.0.3', 'sha256'), 'hex'), NOW() - INTERVAL '4 days'),
('63000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004', 'demo-session-004', encode(digest('127.0.0.4', 'sha256'), 'hex'), NOW() - INTERVAL '1 day');

INSERT INTO notifications (
    id, recipient_id, actor_id, type, title, message, entity_type,
    entity_id, action_url, metadata, is_read, read_at, created_at
) VALUES
(
    '70000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000002',
    'PROMPT_LIKED', 'Prompt của bạn vừa được lưu',
    'Linh Nguyễn đã lưu Spring Boot Code Reviewer.',
    'PROMPT', '30000000-0000-0000-0000-000000000001',
    '/prompts/spring-boot-code-reviewer', '{"promptTitle":"Spring Boot Code Reviewer"}'::jsonb,
    FALSE, NULL, NOW() - INTERVAL '7 days'
),
(
    '70000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000003',
    'PROMPT_COMMENTED', 'Bình luận mới',
    'Minh Trần đã bình luận về prompt của bạn.',
    'PROMPT', '30000000-0000-0000-0000-000000000001',
    '/prompts/spring-boot-code-reviewer#comments', '{"commentId":"62000000-0000-0000-0000-000000000001"}'::jsonb,
    TRUE, NOW() - INTERVAL '2 days', NOW() - INTERVAL '3 days'
),
(
    '70000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000004',
    '10000000-0000-0000-0000-000000000001',
    'SYSTEM', 'Chào mừng đến với PromptHub',
    'Khám phá prompt công khai hoặc tạo collection đầu tiên của bạn.',
    'USER', '10000000-0000-0000-0000-000000000004',
    '/explore', '{}'::jsonb,
    FALSE, NULL, NOW() - INTERVAL '20 days'
);

INSERT INTO activity_logs (
    id, workspace_id, actor_id, action, entity_type, entity_id,
    entity_name, old_values, new_values, ip_hash, user_agent, created_at
) VALUES
(
    '71000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    'PROMPT_CREATED', 'PROMPT', '30000000-0000-0000-0000-000000000001',
    'Spring Boot Code Reviewer', NULL,
    '{"status":"DRAFT","visibility":"PRIVATE"}'::jsonb,
    encode(digest('127.0.0.1', 'sha256'), 'hex'), 'PromptHub Seed Client',
    NOW() - INTERVAL '45 days'
),
(
    '71000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    'PROMPT_PUBLISHED', 'PROMPT', '30000000-0000-0000-0000-000000000001',
    'Spring Boot Code Reviewer',
    '{"status":"DRAFT","visibility":"PRIVATE"}'::jsonb,
    '{"status":"PUBLISHED","visibility":"PUBLIC"}'::jsonb,
    encode(digest('127.0.0.1', 'sha256'), 'hex'), 'PromptHub Seed Client',
    NOW() - INTERVAL '40 days'
),
(
    '71000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'MEMBER_ROLE_CHANGED', 'WORKSPACE_MEMBER', '21000000-0000-0000-0000-000000000003',
    'Linh Nguyễn', '{"role":"EDITOR"}'::jsonb, '{"role":"ADMIN"}'::jsonb,
    encode(digest('127.0.0.2', 'sha256'), 'hex'), 'PromptHub Seed Client',
    NOW() - INTERVAL '15 days'
);

INSERT INTO workspace_api_keys (
    id, workspace_id, created_by, name, key_prefix, key_hash,
    permissions, last_used_at, expires_at, created_at
) VALUES
(
    '72000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    'Local Development Key', 'ph_dev_demo',
    encode(digest('ph_dev_demo_not_a_real_secret', 'sha256'), 'hex'),
    '["prompts:read","prompts:run"]'::jsonb,
    NOW() - INTERVAL '1 day', NOW() + INTERVAL '180 days', NOW() - INTERVAL '30 days'
);

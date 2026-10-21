ALTER TABLE prompt_versions ADD COLUMN documentation jsonb NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE prompt_versions ADD CONSTRAINT prompt_version_documentation_array CHECK (jsonb_typeof(documentation) = 'array');

UPDATE prompt_versions SET documentation = $docs$[
  {"title":"Purpose","content":"Review Spring Boot code for security, performance, database access, and maintainability issues."},
  {"title":"How to use","content":"1. Paste Java source into {{code}}.\n2. Set {{focus}} to All, Security, Performance, Clean Code, or Database.\n3. Include related DTOs, repository queries, and authorization rules.\n4. Run the prompt and verify the suggested fixes."},
  {"title":"Expected outputs","content":"Prioritized issues with severity, explanation, and suggested fixes, including code examples when useful."},
  {"title":"Best practices","content":"Review one focused feature at a time. Include Java and Spring Boot versions and intended behavior. Remove credentials from code samples."},
  {"title":"Limitations","content":"The model only sees the supplied code. Confirm findings with tests and review; it cannot inspect omitted files or verify runtime behavior."}
]$docs$::jsonb WHERE id = '31000000-0000-0000-0000-000000000002';

UPDATE prompt_versions SET documentation = $docs$[
  {"title":"Purpose","content":"Evaluate visual hierarchy, spacing, typography, accessibility, and user flow."},
  {"title":"How to use","content":"Describe the screen, target users, and task they need to complete. Fill in the Variables tab fields. Include device constraints and observed usability problems."},
  {"title":"Expected outputs","content":"Prioritized UX/UI findings with their impact and concrete changes for designers and developers."},
  {"title":"Best practices","content":"Include layout, label text, contrast, and interaction states. Define a measurable goal such as improving checkout completion."},
  {"title":"Limitations","content":"A text description may omit visual details. Validate accessibility with appropriate tools and test usability with representative users."}
]$docs$::jsonb WHERE id = '31000000-0000-0000-0000-000000000003';

UPDATE prompt_versions SET documentation = $docs$[
  {"title":"Purpose","content":"Create an SEO article outline and draft for a target keyword and audience."},
  {"title":"How to use","content":"1. Enter the search term in {{keyword}}.\n2. Describe readers in {{audience}}.\n3. Set {{word_count}} to the approximate article length.\n4. Review the outline, draft, and metadata before publishing."},
  {"title":"Expected outputs","content":"An article title, structured headings, reader-focused draft, and suggested meta description."},
  {"title":"Best practices","content":"Choose a specific search intent. Add original examples, verified sources, and subject expertise. Use keywords naturally."},
  {"title":"Limitations","content":"No live keyword volumes or guaranteed search rankings. Verify factual claims, source links, and current information before publishing."}
]$docs$::jsonb WHERE id = '31000000-0000-0000-0000-000000000004';

UPDATE prompt_versions SET documentation = $docs$[
  {"title":"Purpose","content":"Restyle a reference landscape while preserving its horizon, camera angle, and subject placement."},
  {"title":"How to use","content":"1. Supply a reference image to an image-capable tool.\n2. Fill in {{subject}}, {{style}}, and {{lighting}}.\n3. Set {{aspect_ratio}}, for example 16:9.\n4. Use the completed prompt alongside the reference image in your generation tool."},
  {"title":"Expected outputs","content":"An image following the reference composition with the requested style, lighting, and framing."},
  {"title":"Best practices","content":"Use a clear reference and a coherent visual direction. Refine lighting and style in small steps."},
  {"title":"Limitations","content":"Requires a model that accepts reference images. Composition and aspect-ratio support depend on the tool. Preview images illustrate visual direction rather than guaranteed results."}
]$docs$::jsonb WHERE id = '31000000-0000-0000-0000-000000000007';

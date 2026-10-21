ALTER TABLE prompt_versions ADD COLUMN examples jsonb NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE prompt_versions ADD CONSTRAINT prompt_version_examples_array CHECK (jsonb_typeof(examples) = 'array');

-- Examples belong to the published demo version, rather than every prompt.
UPDATE prompt_versions SET examples = $examples$[
  {"id":"review-validation","title":"Review request validation","input":"code: @PostMapping(\"/users\") public User create(@RequestBody User user) { return repository.save(user); }\nfocus: Security","output":"Issue: The endpoint accepts and returns the persistence entity directly.\nSeverity: High if callers can set privileged fields.\nReason: A request may bind fields such as role or enabled without explicit authorization.\nSuggested fix: Accept a CreateUserRequest DTO with validated name and email fields, use @Valid, assign roles in the service, and return a UserResponse DTO."},
  {"id":"review-pagination","title":"Review an unbounded query","input":"code: @GetMapping(\"/users\") public List<User> list() { return repository.findAll(); }\nfocus: Performance","output":"Issue: The query loads every user into memory.\nSeverity: Medium; impact grows with the table size.\nSuggested fix: Accept a Pageable parameter with a maximum page size and deterministic sorting. Fetch only the fields needed by the response and return a paginated DTO. Verify authorization before exposing user data."}
]$examples$::jsonb WHERE id = '31000000-0000-0000-0000-000000000002';

UPDATE prompt_versions SET examples = $examples$[
  {"id":"ui-checkout","title":"Improve a mobile checkout","input":"Screen: Mobile checkout with six required fields, pale gray field labels, and a small Pay button below the fold.\nGoal: Make checkout easier to complete.","output":"1. Hierarchy: Place the order total and primary Pay button near the final form step.\n2. Form effort: Keep only essential fields and offer appropriate autofill attributes.\n3. Accessibility: Increase label contrast, keep visible labels, and connect errors to their fields.\n4. Interaction: Use a comfortably sized touch target and display a loading state after submission.\nValidate the changes with keyboard navigation and a mobile usability test."}
]$examples$::jsonb WHERE id = '31000000-0000-0000-0000-000000000003';

UPDATE prompt_versions SET examples = $examples$[
  {"id":"seo-coffee","title":"Plan a beginner coffee guide","input":"keyword: how to make cold brew at home\naudience: beginners\nword_count: 800","output":"Title: How to Make Cold Brew at Home: A Beginner Guide\nSearch intent: Learn a simple preparation method.\nOutline:\nH2: Ingredients and equipment\nH2: Choosing a coffee-to-water ratio\nH2: Steeping and filtering\nH2: Serving and storing your cold brew\nH2: Common mistakes\nMeta description: Learn how to make cold brew at home with simple equipment, practical brewing steps, and tips for a smooth cup."}
]$examples$::jsonb WHERE id = '31000000-0000-0000-0000-000000000004';

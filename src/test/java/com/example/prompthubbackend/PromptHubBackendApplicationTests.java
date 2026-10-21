package com.example.prompthubbackend;

import com.example.prompthubbackend.service.PromptExploreService;
import com.example.prompthubbackend.service.PromptCreateService;
import com.example.prompthubbackend.dto.request.CreatePromptRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class PromptHubBackendApplicationTests {

    @Autowired
    private PromptExploreService promptExploreService;

    @Autowired
    private PromptCreateService promptCreateService;

    @Test
    void contextLoads() {
    }

    @Test
    void explorePromptsCanBeLoaded() {
        var prompts = promptExploreService.getPublicPrompts();
        assertNotNull(prompts);
        if (!prompts.isEmpty()) {
            var detail = promptExploreService.getPublicPrompt(prompts.getFirst().id());
            assertNotNull(detail);
            assertFalse(detail.snippet().contains("\\n"));
        }
    }

    @Test
    void examplesBelongToTheSelectedPromptVersion() {
        var reviewer = promptExploreService.getPublicPrompt(java.util.UUID.fromString("30000000-0000-0000-0000-000000000001"));
        org.junit.jupiter.api.Assertions.assertEquals(2, reviewer.examples().size());
        org.junit.jupiter.api.Assertions.assertEquals("review-validation", reviewer.examples().getFirst().id());
        var designer = promptExploreService.getPublicPrompt(java.util.UUID.fromString("30000000-0000-0000-0000-000000000002"));
        org.junit.jupiter.api.Assertions.assertEquals("ui-checkout", designer.examples().getFirst().id());
        var image = promptExploreService.getPublicPrompt(java.util.UUID.fromString("30000000-0000-0000-0000-000000000006"));
        org.junit.jupiter.api.Assertions.assertTrue(image.examples().isEmpty());
    }

    @Test
    void documentationBelongsToTheSelectedPromptVersion() {
        var reviewer = promptExploreService.getPublicPrompt(java.util.UUID.fromString("30000000-0000-0000-0000-000000000001"));
        var designer = promptExploreService.getPublicPrompt(java.util.UUID.fromString("30000000-0000-0000-0000-000000000002"));
        org.junit.jupiter.api.Assertions.assertEquals(5, reviewer.documentation().size());
        org.junit.jupiter.api.Assertions.assertTrue(reviewer.documentation().getFirst().content().contains("Spring Boot"));
        org.junit.jupiter.api.Assertions.assertTrue(designer.documentation().getFirst().content().contains("visual hierarchy"));
        org.junit.jupiter.api.Assertions.assertTrue(reviewer.documentation().get(1).content().contains("\n"));
    }

    @Test
    @Transactional
    void promptCanBeCreatedWithJsonConfiguration() {
        var request = new CreatePromptRequest(
                "Integration test prompt", "Created by an integration test", "Private",
                "GPT-5", "English", List.of("Java", "Testing"),
                "Review {{source_code}}", "You are a code reviewer.",
                List.of(new CreatePromptRequest.PromptVariableRequest(
                        "source_code", "Source code", "Long Text", true, "Paste code", List.of()
                )),
                "Initial version", false, "text", null, null
        );
        var created = promptCreateService.create("ducngo", request);
        assertNotNull(created.id());
        assertNotNull(created.version());
    }

}

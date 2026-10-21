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

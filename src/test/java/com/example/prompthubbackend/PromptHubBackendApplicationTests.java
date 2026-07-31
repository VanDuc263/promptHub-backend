package com.example.prompthubbackend;

import com.example.prompthubbackend.service.PromptExploreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PromptHubBackendApplicationTests {

    @Autowired
    private PromptExploreService promptExploreService;

    @Test
    void contextLoads() {
    }

    @Test
    void explorePromptsCanBeLoaded() {
        assertNotNull(promptExploreService.getPublicPrompts());
    }

}

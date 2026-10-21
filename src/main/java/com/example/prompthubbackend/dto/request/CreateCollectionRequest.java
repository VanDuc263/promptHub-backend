package com.example.prompthubbackend.dto.request;

import com.example.prompthubbackend.model.enums.CollectionVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCollectionRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @Pattern(regexp = "^/collection-covers/[a-z0-9-]+\\.(svg|png|webp|jpg|jpeg)$", message = "must be a PromptHub system cover")
        String coverImageUrl,
        @NotNull CollectionVisibility visibility,
        java.util.UUID workspaceId
) {
}

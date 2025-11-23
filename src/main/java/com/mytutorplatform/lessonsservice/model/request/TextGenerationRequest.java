package com.mytutorplatform.lessonsservice.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TextGenerationRequest(
        @NotBlank String prompt,
        String existingText,
        String lessonTitle,
        String level
) {
}

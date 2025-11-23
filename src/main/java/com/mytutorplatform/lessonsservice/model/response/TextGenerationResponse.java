package com.mytutorplatform.lessonsservice.model.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record TextGenerationResponse(
        String html,
        Map<String, Object> meta
) {
}

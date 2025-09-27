package com.mytutorplatform.lessonsservice.model.request;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TranscriptGenerateRequest(
        List<UUID> wordIds,
        Integer maxWords,
        String theme,
        String cefr,
        String language,
        String style,
        Integer seed,
        Map<String,Object> constraints
) {}

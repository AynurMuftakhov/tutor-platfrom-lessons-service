package com.mytutorplatform.lessonsservice.model.request;

import java.util.List;
import java.util.UUID;

public record TranscriptManualCreateRequest(
        String transcript,
        List<UUID> wordIds,
        String language,
        String theme,
        String cefr,
        String style
) {}

package com.mytutorplatform.lessonsservice.model.response;

import java.util.Map;
import java.util.UUID;

public record TranscriptResponse(
        UUID transcriptId,
        String transcript,
        Map<String, Boolean> wordCoverage,
        Integer estimatedDurationSec,
        Map<String, Object> metadata
) {}

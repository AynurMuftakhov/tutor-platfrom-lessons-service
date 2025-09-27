package com.mytutorplatform.lessonsservice.model.response;

import java.time.Instant;
import java.util.UUID;

public record JobStatusResponse(
        UUID jobId,
        String status,
        UUID audioMaterialId,
        String audioUrl,
        Integer durationSec,
        String transcript,
        Instant createdAt,
        Instant updatedAt
) {}

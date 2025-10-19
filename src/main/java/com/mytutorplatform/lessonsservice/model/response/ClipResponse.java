package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.ClipMetadata;
import lombok.Builder;

import java.net.URI;

@Builder
public record ClipResponse(
    String clipId,
    String lessonId,
    String turnId,
    String mime,
    long sizeBytes,
    Long durationMs,
    long expiresAt,
    String url
) {
  public static ClipResponse from(ClipMetadata metadata) {
    return ClipResponse.builder()
        .clipId(metadata.getClipId())
        .lessonId(metadata.getLessonId())
        .turnId(metadata.getTurnId())
        .mime(metadata.getMime())
        .sizeBytes(metadata.getSizeBytes())
        .durationMs(metadata.getDurationMs())
        .expiresAt(metadata.getExpiresAt() != null ? metadata.getExpiresAt().toEpochMilli() : 0L)
        .url("/api/lesson-clips/" + metadata.getClipId())
        .build();
  }

  public URI resourceUri() {
    return URI.create(url);
  }
}

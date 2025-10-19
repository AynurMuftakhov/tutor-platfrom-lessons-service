package com.mytutorplatform.lessonsservice.model;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class ClipMetadata {
  String clipId;
  String lessonId;
  String turnId;
  String ownerUserId;
  String mime;
  long sizeBytes;
  Instant createdAt;
  Instant expiresAt;
  Path absPath;
  Long durationMs;
  String sha256;

  public boolean isExpired(Instant now) {
    return expiresAt != null && expiresAt.isBefore(now);
  }
}

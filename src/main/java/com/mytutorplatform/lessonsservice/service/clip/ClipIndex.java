package com.mytutorplatform.lessonsservice.service.clip;

import com.mytutorplatform.lessonsservice.model.ClipMetadata;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClipIndex {
  private final ConcurrentHashMap<String, ClipMetadata> byId = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Set<String>> byLesson = new ConcurrentHashMap<>();

  public ClipMetadata put(ClipMetadata metadata) {
    byId.put(metadata.getClipId(), metadata);
    byLesson
        .computeIfAbsent(metadata.getLessonId(), key -> ConcurrentHashMap.newKeySet())
        .add(metadata.getClipId());
    return metadata;
  }

  public ClipMetadata get(String clipId) {
    return byId.get(clipId);
  }

  public ClipMetadata remove(String clipId) {
    ClipMetadata removed = byId.remove(clipId);
    if (removed != null) {
      Set<String> lessonSet = byLesson.get(removed.getLessonId());
      if (lessonSet != null) {
        lessonSet.remove(clipId);
        if (lessonSet.isEmpty()) {
          byLesson.remove(removed.getLessonId());
        }
      }
    }
    return removed;
  }

  public List<ClipMetadata> removeByLesson(String lessonId) {
    Set<String> clipIds = byLesson.remove(lessonId);
    if (clipIds == null || clipIds.isEmpty()) {
      return List.of();
    }
    List<ClipMetadata> removed = new ArrayList<>();
    for (String clipId : clipIds) {
      ClipMetadata meta = byId.remove(clipId);
      if (meta != null) {
        removed.add(meta);
      }
    }
    return removed;
  }

  public List<ClipMetadata> findExpired(Instant now) {
    List<ClipMetadata> expired = new ArrayList<>();
    for (ClipMetadata metadata : byId.values()) {
      if (metadata.isExpired(now)) {
        expired.add(metadata);
      }
    }
    return expired;
  }
}

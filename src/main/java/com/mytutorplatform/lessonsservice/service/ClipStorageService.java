package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.config.ClipProperties;
import com.mytutorplatform.lessonsservice.model.ClipMetadata;
import com.mytutorplatform.lessonsservice.service.clip.ClipIndex;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClipStorageService {
  private static final Logger log = LoggerFactory.getLogger(ClipStorageService.class);

  private final ClipProperties properties;
  private final ClipIndex index;
  private final Path baseDir;
  private final Set<String> allowedMimeTypes;

  public ClipStorageService(ClipProperties properties, ClipIndex index) throws IOException {
    this.properties = properties;
    this.index = index;
    this.baseDir = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize();
    Files.createDirectories(this.baseDir);
    this.allowedMimeTypes = properties.getAllowedContentTypes().stream()
        .filter(StringUtils::hasText)
        .map(this::normalizeMime)
        .collect(Collectors.toUnmodifiableSet());
  }

  @PostConstruct
  public void initialize() {
    try {
      Instant cutoff = Instant.now().minusSeconds(Math.max(1, properties.getTtlSeconds()));
      if (!Files.exists(this.baseDir)) {
        return;
      }
      Files.walk(this.baseDir)
          .filter(Files::isRegularFile)
          .forEach(path -> {
            try {
              Instant lastModified = Files.getLastModifiedTime(path).toInstant();
              if (lastModified.isBefore(cutoff)) {
                Files.deleteIfExists(path);
              }
            } catch (IOException ex) {
              log.warn("Failed cleaning stale clip file path={} ", path, ex);
            }
          });
    } catch (IOException ex) {
      log.warn("Failed scanning clip base directory for cleanup", ex);
    }
  }

  public ClipMetadata saveClip(String lessonId,
                               String turnId,
                               String ownerUserId,
                               String contentType,
                               Long durationMs,
                               byte[] bytes) throws IOException {
    if (bytes == null || bytes.length == 0) {
      throw new IllegalArgumentException("Clip content is required");
    }

    if (properties.getMaxBytes() > 0 &&  bytes.length > properties.getMaxBytes()) {
      throw new IllegalArgumentException("Clip exceeds max size");
    }

    String normalizedMime = normalizeMime(contentType);
    if (!allowedMimeTypes.contains(normalizedMime)) {
      throw new IllegalArgumentException("Content-Type not allowed: " + contentType);
    }

    String clipId = generateClipId();
    String extension = resolveExtension(normalizedMime);
    Path lessonDir = baseDir.resolve(lessonId);
    Files.createDirectories(lessonDir);
    Path target = lessonDir.resolve(clipId + extension);

    Path tempFile = Files.createTempFile(baseDir, "clip-upload-", ".tmp");
    Files.write(tempFile, bytes);
    try {
      Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException moveEx) {
      Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    }

    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(Math.max(1, properties.getTtlSeconds()));

    ClipMetadata metadata = ClipMetadata.builder()
        .clipId(clipId)
        .lessonId(lessonId)
        .turnId(turnId)
        .ownerUserId(ownerUserId)
        .mime(normalizedMime)
        .sizeBytes(bytes.length)
        .createdAt(now)
        .expiresAt(expiresAt)
        .absPath(target)
        .durationMs(durationMs)
        .sha256(null)
        .build();
    index.put(metadata);
    log.info("Saved lesson clip clipId={} lessonId={} turnId={} sizeBytes={}", clipId, lessonId, turnId, bytes.length);
    return metadata;
  }

  public ClipMetadata getClip(String clipId) {
    return index.get(clipId);
  }

  public Resource getClipResource(ClipMetadata metadata) {
    return new PathResource(metadata.getAbsPath());
  }

  public boolean deleteClip(String clipId) {
    ClipMetadata removed = index.remove(clipId);
    if (removed == null) {
      return false;
    }
    try {
      Files.deleteIfExists(removed.getAbsPath());
    } catch (IOException ex) {
      log.warn("Failed deleting clip file clipId={} path={}", clipId, removed.getAbsPath(), ex);
    }
    return true;
  }

  public int deleteByLesson(String lessonId) {
    return index.removeByLesson(lessonId).stream()
        .mapToInt(meta -> {
          try {
            Files.deleteIfExists(meta.getAbsPath());
            return 1;
          } catch (IOException ex) {
            log.warn("Failed deleting clip file clipId={} path={}", meta.getClipId(), meta.getAbsPath(), ex);
            return 0;
          }
        })
        .sum();
  }

  public int purgeExpired() {
    Instant now = Instant.now();
    return index.findExpired(now).stream()
        .mapToInt(meta -> {
          if (index.remove(meta.getClipId()) != null) {
            try {
              Files.deleteIfExists(meta.getAbsPath());
            } catch (IOException ex) {
              log.warn("Failed deleting expired clip file clipId={} path={}", meta.getClipId(), meta.getAbsPath(), ex);
            }
            return 1;
          }
          return 0;
        })
        .sum();
  }

  private String normalizeMime(String mime) {
    if (!StringUtils.hasText(mime)) return "";
    MediaType mt = MediaType.parseMediaType(mime);
    String base = (mt.getType() + "/" + mt.getSubtype()).toLowerCase(Locale.ROOT);

    String codecs = mt.getParameter("codecs");
    if (StringUtils.hasText(codecs)) {
      return base + ";codecs=" + codecs.toLowerCase(Locale.ROOT);
    }
    return base;
  }

  private String resolveExtension(String normalizedMime) {
    if (normalizedMime.contains("webm")) return ".webm";
    if (normalizedMime.contains("ogg")) return ".ogg";
    if (normalizedMime.contains("wav")) return ".wav";
    return ".bin";
  }

  private String generateClipId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}

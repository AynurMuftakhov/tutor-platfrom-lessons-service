package com.mytutorplatform.lessonsservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;

public interface ImageStorageService {
  record StoredImage(
          String url,
          String fileName,
          String mimeType,
          long sizeBytes,
          Integer width,
          Integer height,
          Instant storedAt
  ) {}

  StoredImage store(MultipartFile file);

  /**
   * Lists absolute paths to stored image files, sorted newest first, paginated.
   */
  record Page(java.util.List<Path> items, int total) {}

  Page list(int offset, int limit);
}

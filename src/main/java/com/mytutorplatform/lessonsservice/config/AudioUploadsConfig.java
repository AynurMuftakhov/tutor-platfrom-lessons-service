package com.mytutorplatform.lessonsservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AudioUploadsConfig {
  private static final Logger log = LoggerFactory.getLogger(AudioUploadsConfig.class);

  @Value("${uploads.audio.dir:./uploads/audio}")
  private String uploadsDir;

  public Path getUploadsPath() {
    return Paths.get(uploadsDir);
  }

  public void ensureUploadsDirectory() {
    Path path = getUploadsPath();
    try {
      Files.createDirectories(path);
      log.info("Audio uploads directory ready at: {}", path.toAbsolutePath());
    } catch (Exception e) {
      log.error("Failed to create audio uploads directory at {}", path.toAbsolutePath(), e);
    }
  }
}

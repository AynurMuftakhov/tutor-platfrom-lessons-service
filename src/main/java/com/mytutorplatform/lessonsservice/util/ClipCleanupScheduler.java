package com.mytutorplatform.lessonsservice.util;

import com.mytutorplatform.lessonsservice.service.ClipStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClipCleanupScheduler {
  private static final Logger log = LoggerFactory.getLogger(ClipCleanupScheduler.class);
  private final ClipStorageService storageService;

  public ClipCleanupScheduler(ClipStorageService storageService) {
    this.storageService = storageService;
  }

  @Scheduled(fixedDelay = 60_000L)
  public void purgeExpiredClips() {
    int purged = storageService.purgeExpired();
    if (purged > 0) {
      log.info("Purged {} expired lesson clips", purged);
    }
  }
}

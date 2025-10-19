package com.mytutorplatform.lessonsservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadsConfig {

    private static final Logger log = LoggerFactory.getLogger(UploadsConfig.class);

    @Value("${uploads.dir:./uploads/images}")
    private String uploadsDir;

    public Path getUploadsPath() {
        return Paths.get(uploadsDir);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureUploadsDirectory() {
        Path path = getUploadsPath();
        try {
            Files.createDirectories(path);
            log.info("Uploads directory ready at: {}", path.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create uploads directory at {}", path.toAbsolutePath(), e);
        }
    }
}

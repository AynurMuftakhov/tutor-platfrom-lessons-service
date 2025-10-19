package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.config.AudioUploadsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalAudioStorageService {

    private final AudioUploadsConfig config;

    public StoredAudio store(byte[] data, String ext) {
        if (data == null || data.length == 0) {
            throw new StorageException("Audio bytes are required", 400);
        }
        try {
            config.ensureUploadsDirectory();
            Path base = config.getUploadsPath().toAbsolutePath().normalize();
            String today = todayPath();
            Path dated = base.resolve(today);
            Files.createDirectories(dated);

            String extension = (ext == null || ext.isBlank()) ? ".mp3" : (ext.startsWith(".") ? ext : "." + ext);
            String fileName = UUID.randomUUID() + extension;
            Path target = dated.resolve(fileName);
            try (OutputStream os = Files.newOutputStream(target)) {
                os.write(data);
            }

            String rel = dated.toString()
                    .replace(base.toString(), "")
                    .replace('\\', '/');
            if (rel.startsWith("/")) rel = rel.substring(1);
            String url = "/uploads/audio/" + (rel.isBlank() ? "" : (rel + "/")) + fileName;
            return new StoredAudio(url, fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE, data.length, Instant.now());
        } catch (Exception e) {
            log.error("Failed to store audio: {}", e.getMessage(), e);
            throw new StorageException("Failed to store audio", 500);
        }
    }

    private String todayPath() {
        var now = Instant.now().atZone(ZoneOffset.UTC);
        return "%04d/%02d/%02d".formatted(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    public record StoredAudio(String url, String fileName, String mimeType, long sizeBytes, Instant createdAt) {}

    public static class StorageException extends RuntimeException {
        public final int status;
        public StorageException(String msg, int status) { super(msg); this.status = status; }
    }
}

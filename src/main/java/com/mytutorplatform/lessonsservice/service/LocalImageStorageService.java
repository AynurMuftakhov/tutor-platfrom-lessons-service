package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.config.UploadsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalImageStorageService implements ImageStorageService {

  private static final Set<String> ALLOWED_MIME = Set.of(
          MediaType.IMAGE_PNG_VALUE,
          MediaType.IMAGE_JPEG_VALUE,
          "image/webp",
          MediaType.IMAGE_GIF_VALUE
  );

  private static final Map<String, String> EXT_TO_MIME = Map.of(
          "png", MediaType.IMAGE_PNG_VALUE,
          "jpg", MediaType.IMAGE_JPEG_VALUE,
          "jpeg", MediaType.IMAGE_JPEG_VALUE,
          "webp", "image/webp",
          "gif", MediaType.IMAGE_GIF_VALUE
  );

  private final UploadsConfig config;

  private Path baseDir() {
    return config.getUploadsPath().toAbsolutePath().normalize();
  }

  private String todayPath() {
    Instant now = Instant.now();
    var z = now.atZone(ZoneOffset.UTC);
    return "%04d/%02d/%02d".formatted(z.getYear(), z.getMonthValue(), z.getDayOfMonth());
  }

  private String extensionForMime(String mime, String originalName) {
    String ext = StringUtils.getFilenameExtension(originalName);
    String extLower = ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    if (EXT_TO_MIME.containsKey(extLower) && Objects.equals(EXT_TO_MIME.get(extLower), mime)) {
      return "." + extLower;
    }
    // Derive from MIME
    return EXT_TO_MIME.entrySet().stream()
            .filter(e -> e.getValue().equals(mime))
            .map(e -> "." + e.getKey())
            .findFirst()
            .orElse("");
  }

  private Optional<int[]> probeDimensions(Path image, String extLower) {
    try {
      // ImageIO supports png/jpg/gif by default. For webp, add TwelveMonkeys.
      if (Set.of("png", "jpg", "jpeg", "gif").contains(extLower)) {
        BufferedImage img = ImageIO.read(image.toFile());
        if (img != null) return Optional.of(new int[]{img.getWidth(), img.getHeight()});
      }
    } catch (Exception ignored) {}
    return Optional.empty();
  }

  @Override
  public StoredImage store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new StorageException("File is required", 400);
    }
    long max = 10L * 1024 * 1024;
    if (file.getSize() >  max) {
      throw new StorageException("File too large", 413);
    }

    String mime = Optional.ofNullable(file.getContentType()).orElse("");
    if (!ALLOWED_MIME.contains(mime)) {
      throw new StorageException("Unsupported file type", 400);
    }

    try {
      Path base = baseDir();
      Path dated = base.resolve(todayPath());
      Files.createDirectories(dated);

      String ext = extensionForMime(mime, Objects.requireNonNullElse(file.getOriginalFilename(), ""));
      String uuid = UUID.randomUUID().toString();
      String fileName = uuid + ext;

      // Write to temp then atomic move
      Path tmp = Files.createTempFile(dated, "upload-", ".tmp");
      try (InputStream in = file.getInputStream()) {
        Files.copy(in, tmp, REPLACE_EXISTING);
      }

      Path target = dated.resolve(fileName);
      try {
        Files.move(tmp, target, ATOMIC_MOVE, REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(tmp, target, REPLACE_EXISTING);
      }

      String relUrl = dated.toAbsolutePath().normalize().toString()
              .replace(base.toAbsolutePath().normalize().toString(), "")
              .replace('\\', '/');
      if (relUrl.startsWith("/")) relUrl = relUrl.substring(1);
      String url = "/uploads/images/" + (relUrl.isBlank() ? "" : (relUrl + "/")) + fileName;

      String extLower = ext.startsWith(".") ? ext.substring(1).toLowerCase(Locale.ROOT) : ext.toLowerCase(Locale.ROOT);
      Optional<int[]> dims = probeDimensions(target, extLower);

      return new StoredImage(
              url,
              fileName,
              mime,
              file.getSize(),
              dims.map(d -> d[0]).orElse(null),
              dims.map(d -> d[1]).orElse(null),
              Instant.now()
      );
    } catch (Exception ex) {
      log.error("Failed to store image: {}", ex.getMessage(), ex);
      throw new StorageException("Failed to store file", 500);
    }
  }

  @Override
  public Page list(int offset, int limit) {
    try {
      Path base = baseDir();
      if (!Files.exists(base)) {
        Files.createDirectories(base);
        return new Page(List.of(), 0);
      }

      // Walk up to depth 4 to cover yyyy/MM/dd
      List<Path> all = new ArrayList<>();
      try (var stream = Files.walk(base, 4)) {
        stream.filter(Files::isRegularFile).forEach(all::add);
      }

      // Sort by last modified desc
      all.sort((a, b) -> {
        try {
          return Long.compare(
                  Files.getLastModifiedTime(b).toMillis(),
                  Files.getLastModifiedTime(a).toMillis()
          );
        } catch (Exception e) {
          return 0;
        }
      });

      int total = all.size();
      int from = Math.max(0, Math.min(offset, total));
      int to = Math.max(from, Math.min(from + Math.max(0, limit), total));
      return new Page(all.subList(from, to), total);
    } catch (Exception ex) {
      log.error("Failed to list images: {}", ex.getMessage(), ex);
      throw new StorageException("Failed to list images", 500);
    }
  }

  public static class StorageException extends RuntimeException {
    public final int status;
    public StorageException(String msg, int status) { super(msg); this.status = status; }
  }
}

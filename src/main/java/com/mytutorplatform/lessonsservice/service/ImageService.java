package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.response.ImageAssetItem;
import com.mytutorplatform.lessonsservice.model.response.ImageAssetsPage;
import com.mytutorplatform.lessonsservice.model.response.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService {

  private static final Map<String, String> EXTENSION_TO_MIME = Map.of(
          "png", MediaType.IMAGE_PNG_VALUE,
          "jpg", MediaType.IMAGE_JPEG_VALUE,
          "jpeg", MediaType.IMAGE_JPEG_VALUE,
          "webp", "image/webp",
          "gif", MediaType.IMAGE_GIF_VALUE
  );

  private final ImageStorageService storage;

  public ImageUploadResponse uploadImage(MultipartFile file) {
    var stored = storage.store(file);
    return ImageUploadResponse.builder()
            .url(stored.url())
            .fileName(stored.fileName())
            .mimeType(stored.mimeType())
            .sizeBytes(stored.sizeBytes())
            .width(stored.width())
            .height(stored.height())
            .createdAt(DateTimeFormatter.ISO_INSTANT.format(stored.storedAt().atOffset(ZoneOffset.UTC)))
            .build();
  }

  public ImageAssetsPage listAssets(int offset, int limit) {
    var page = storage.list(offset, limit);
    DateTimeFormatter iso = DateTimeFormatter.ISO_INSTANT;

    List<ImageAssetItem> items = new ArrayList<>();
    for (Path path : page.items()) {
      try {
        String fileName = path.getFileName().toString();
        String ext = Optional.ofNullable(StringUtils.getFilenameExtension(fileName)).orElse("").toLowerCase(Locale.ROOT);
        String mime = EXTENSION_TO_MIME.getOrDefault(ext, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        long size = Files.size(path);
        String createdAt = iso.format(Files.getLastModifiedTime(path).toInstant().atOffset(ZoneOffset.UTC));

        // Build URL relative to /uploads/images/**
        Deque<String> segments = new ArrayDeque<>();
        segments.addFirst(fileName);
        Path current = path.getParent();
        while (current != null && current.getFileName() != null) {
          String name = current.getFileName().toString();
          segments.addFirst(name);
          if ("images".equals(name)) break;
          current = current.getParent();
        }
        String rel = String.join("/", segments);
        String url = "/uploads/" + rel;

        items.add(ImageAssetItem.builder()
                .url(url)
                .fileName(fileName)
                .mimeType(mime)
                .sizeBytes(size)
                .width(null)
                .height(null)
                .createdAt(createdAt)
                .build());
      } catch (Exception ignored) {}
    }
    return ImageAssetsPage.builder()
            .total(page.total())
            .items(items)
            .build();
  }
}

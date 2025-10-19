package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.ClipMetadata;
import com.mytutorplatform.lessonsservice.model.response.ClipResponse;
import com.mytutorplatform.lessonsservice.service.ClipStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.io.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/api")
public class ClipController {
  private static final Logger log = LoggerFactory.getLogger(ClipController.class);
  private final ClipStorageService storageService;

  public ClipController(ClipStorageService storageService) {
    this.storageService = storageService;
  }

  @PostMapping(
          path = "/lessons/{lessonId}/turns/{turnId}/clips",
          consumes = MediaType.ALL_VALUE,
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  public ResponseEntity<ClipResponse> uploadClip(@PathVariable String lessonId,
                                                 @PathVariable String turnId,
                                                 @RequestHeader("X-User-Id") String userId,
                                                 @RequestHeader(value = "Content-Type", required = false) String contentType,
                                                 @RequestHeader(value = "X-Clip-Duration-Ms", required = false) Long durationMs,
                                                 @RequestBody byte[] body) {
    try {
      final ClipMetadata metadata = storageService.saveClip(
              lessonId, turnId, userId, contentType, durationMs, body
      );
      final ClipResponse response = ClipResponse.from(metadata);
      return ResponseEntity.created(response.resourceUri()).body(response);
    } catch (IllegalArgumentException ex) {
      throw new ServerWebInputException(ex.getMessage());
    } catch (IOException ex) {
      log.error("Failed to store clip", ex);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store clip");
    }
  }

  @GetMapping(path = "/lesson-clips/{clipId}")
  @ResponseBody
  public ResponseEntity<?> getClip(@PathVariable String clipId, @RequestHeader HttpHeaders headers) {
    ClipMetadata meta = storageService.getClip(clipId);
    if (meta == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clip not found");
    if (meta.isExpired(Instant.now())) {
      storageService.deleteClip(clipId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clip expired");
    }

    Resource resource = storageService.getClipResource(meta);
    try {
      if (!resource.exists()) {
        storageService.deleteClip(clipId);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clip missing");
      }

      long total = resource.contentLength();

      // strip parameters like ;codecs=opus
      MediaType original = MediaType.parseMediaType(meta.getMime());
      MediaType baseType = new MediaType(original.getType(), original.getSubtype());

      List<HttpRange> ranges = headers.getRange();
      if (!ranges.isEmpty()) {
        HttpRange r = ranges.get(0);
        long start = r.getRangeStart(total);
        long end   = Math.min(r.getRangeEnd(total), total - 1);
        int  len   = (int) Math.max(0, end - start + 1);

        byte[] slice = readRange(resource, start, len); // fallback-safe reader (below)

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(baseType)
                .contentLength(slice.length)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + (start + slice.length - 1) + "/" + total)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Range,Content-Length,Content-Type")
                .cacheControl(CacheControl.noCache().cachePrivate().mustRevalidate())
                .body(slice);
      }

      // full body (200); still use baseType to avoid converter surprises on errors
      return ResponseEntity.ok()
              .contentType(baseType)
              .contentLength(total)
              .header(HttpHeaders.ACCEPT_RANGES, "bytes")
              .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Range,Content-Length,Content-Type")
              .cacheControl(CacheControl.noCache().cachePrivate().mustRevalidate())
              .body(resource);

    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read clip", ex);
    }
  }

  /** Reads a byte range from either a File or generic Resource stream. */
  private static byte[] readRange(Resource res, long start, int len) throws IOException {
    try {
      // Fast path for file resources
      File file = res.getFile();
      try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
        byte[] buf = new byte[len];
        raf.seek(start);
        int read = raf.read(buf);
        return (read == len) ? buf : Arrays.copyOf(buf, Math.max(0, read));
      }
    } catch (FileNotFoundException | IllegalStateException e) {
      // Not a plain File; fall back to stream
      try (InputStream in = res.getInputStream();
           ByteArrayOutputStream out = new ByteArrayOutputStream(len)) {
        in.skipNBytes(start); // Java 11+
        byte[] buf = new byte[Math.min(len, 64 * 1024)];
        int remaining = len, n;
        while (remaining > 0 && (n = in.read(buf, 0, Math.min(buf.length, remaining))) != -1) {
          out.write(buf, 0, n);
          remaining -= n;
        }
        return out.toByteArray();
      }
    }
  }

  @DeleteMapping(path = "/lessons/{lessonId}/clips")
  @ResponseBody
  public ResponseEntity<Void> deleteByLesson(@PathVariable String lessonId) {
    int deleted = storageService.deleteByLesson(lessonId);
    if (deleted > 0) {
      log.info("Deleted {} clips for lesson {}", deleted, lessonId);
    }
    return ResponseEntity.noContent().build();
  }
}

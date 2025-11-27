package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.response.ClipResponse;
import com.mytutorplatform.lessonsservice.service.clip.ClipService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
public class ClipController {
  private final ClipService clipService;

  public ClipController(ClipService clipService) {
    this.clipService = clipService;
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
    return clipService.uploadClip(lessonId, turnId, userId, contentType, durationMs, body);
  }

  @GetMapping(path = "/lesson-clips/{clipId}")
  @ResponseBody
  public ResponseEntity<?> getClip(@PathVariable String clipId, @RequestHeader HttpHeaders headers) {
    return clipService.getClip(clipId, headers);
  }

  @DeleteMapping(path = "/lessons/{lessonId}/clips")
  @ResponseBody
  public ResponseEntity<Void> deleteByLesson(@PathVariable String lessonId) {
    return clipService.deleteByLesson(lessonId);
  }
}

package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.response.ImageUploadResponse;
import com.mytutorplatform.lessonsservice.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UploadsController {

  private final ImageService imageService;

  @PostMapping(value = "/api/uploads/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageUploadResponse> upload(@RequestPart("file") MultipartFile file) {
    return ResponseEntity.ok(imageService.uploadImage(file));
  }

  @GetMapping("/api/assets/images")
  public ResponseEntity<?> list(
          @RequestParam(defaultValue = "0") int offset,
          @RequestParam(defaultValue = "24") int limit
  ) {
    return ResponseEntity.ok(imageService.listAssets(offset, limit));
  }
}
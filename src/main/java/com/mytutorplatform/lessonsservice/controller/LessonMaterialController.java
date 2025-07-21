package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.response.LessonMaterialDto;
import com.mytutorplatform.lessonsservice.service.LessonMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons/{lessonId}/materials")
@RequiredArgsConstructor
public class LessonMaterialController {
    private final LessonMaterialService lessonMaterialService;

    @GetMapping
    public List<LessonMaterialDto> list(@PathVariable UUID lessonId) {
        return lessonMaterialService.list(lessonId);
    }

    @PostMapping
    public ResponseEntity<Void> link(@PathVariable UUID lessonId,
                                     @RequestParam UUID materialId) {
        lessonMaterialService.link(lessonId, materialId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{linkId}")
    public void reorder(@PathVariable UUID lessonId,
                        @PathVariable UUID linkId,
                        @RequestBody Map<String, Integer> body) {
        Integer newOrder = body.get("order");
        if (newOrder == null) {
            throw new IllegalArgumentException("Order is required");
        }
        lessonMaterialService.reorder(linkId, newOrder);
    }

    @DeleteMapping("/{linkId}")
    public void unlink(@PathVariable UUID lessonId,
                       @PathVariable UUID linkId) {
        lessonMaterialService.unlink(linkId);
    }
}
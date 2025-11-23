package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.response.LessonContentLinkDto;
import com.mytutorplatform.lessonsservice.service.LessonContentLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons/{lessonId}/lesson-contents")
@RequiredArgsConstructor
public class LessonContentLinkController {

    private final LessonContentLinkService linkService;

    @GetMapping
    public List<LessonContentLinkDto> list(@PathVariable UUID lessonId) {
        return linkService.list(lessonId);
    }

    @PostMapping
    public ResponseEntity<Void> link(@PathVariable UUID lessonId,
                                     @RequestParam UUID lessonContentId) {
        linkService.link(lessonId, lessonContentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{linkId}")
    public void unlink(@PathVariable UUID linkId) {
        linkService.unlink(linkId);
    }
}

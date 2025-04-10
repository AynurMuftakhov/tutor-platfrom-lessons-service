package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import com.mytutorplatform.lessonsservice.service.LessonService;
import com.mytutorplatform.lessonsservice.model.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@SuppressWarnings("unused")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping
    public ResponseEntity<Lesson> createLesson(@RequestBody CreateLessonRequest createLessonRequest) {
        return ResponseEntity.ok(lessonService.createLesson(createLessonRequest));
    }

    @GetMapping
    public ResponseEntity<Page<Lesson>> getAllLessons(@RequestParam(required = false) UUID tutorId,
                                                      @RequestParam(required = false) UUID studentId,
                                                      @RequestParam(required = false) List<LessonStatus> status,
                                                      @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(lessonService.getAllLessons(tutorId, studentId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable UUID id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable UUID id, @RequestBody UpdateLessonRequest updateLessonRequest) {
        return ResponseEntity.ok(lessonService.updateLesson(id, updateLessonRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}

package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.TutorStatistics;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import com.mytutorplatform.lessonsservice.model.response.LessonLight;
import com.mytutorplatform.lessonsservice.service.LessonService;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class LessonController {

    private final LessonService lessonService;
    private final StatisticsService statisticsService;

    @PostMapping
    public ResponseEntity<Lesson> createLesson(@RequestBody CreateLessonRequest createLessonRequest) {
        return ResponseEntity.ok(lessonService.createLesson(createLessonRequest));
    }

    @GetMapping
    public List<Lesson> getAllLessons(@RequestParam(required = false) UUID tutorId,
                                                      @RequestParam(required = false) UUID studentId,
                                                      @RequestParam(required = false) List<LessonStatus> status,
                                                      @RequestParam(required = false) OffsetDateTime date,
                                                      @RequestParam(required = false) OffsetDateTime startDate,
                                                      @RequestParam(required = false) OffsetDateTime endDate) {
        return lessonService.getAllLessons(tutorId, studentId, status, date, startDate, endDate);
    }

    @GetMapping("/upcoming")
    public List<Lesson> getUpcomingLessons(@RequestParam(required = false) UUID tutorId,
                                           @RequestParam(required = false) UUID studentId,
                                           @RequestParam(required = false) List<LessonStatus> status,
                                           @RequestParam OffsetDateTime currentDate,
                                           @RequestParam(required = false, defaultValue = "2") int limit) {
        return lessonService.getUpcomingLessons(tutorId, studentId, status, currentDate, limit);
    }

    @GetMapping("/mytutor/schedule")
    public List<LessonLight> getMyTutorSchedule(@RequestParam(required = false) UUID tutorId,
                                                @RequestParam(required = false) UUID studentId,
                                                @RequestParam OffsetDateTime startDate,
                                                @RequestParam OffsetDateTime endDate) {
        return lessonService.getMyTutorSchedule(tutorId, studentId, startDate, endDate);
    }

    @GetMapping("/now")
    public Lesson getUpcomingLessons(@RequestParam(required = false) UUID tutorId,
                                     @RequestParam(required = false) UUID studentId,
                                     @RequestParam OffsetDateTime currentDate) {
        return lessonService.getCurrentLesson(tutorId, studentId, currentDate);
    }

    @GetMapping("tutor/{tutorId}/statistics")
    public ResponseEntity<TutorStatistics> getTutorStatistics(@PathVariable UUID tutorId){
        return ResponseEntity.ok(statisticsService.getTutorStatistics(tutorId));
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

    @GetMapping("/month-counts")
    public ResponseEntity<Map<String, Integer>> getLessonCountsByMonth(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID tutorId) {
        return ResponseEntity.ok(lessonService.getLessonCountsByMonth(year, month, studentId, tutorId));
    }
}

package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.response.LessonSummaryItem;
import com.mytutorplatform.lessonsservice.model.response.StudentDashboardMetricsSummary;
import com.mytutorplatform.lessonsservice.model.response.TutorDashboardActionsSummary;
import com.mytutorplatform.lessonsservice.service.LessonDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons/dashboard")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class LessonDashboardController {

    private final LessonDashboardService lessonDashboardService;

    @GetMapping("/next")
    public ResponseEntity<LessonSummaryItem> getNextLesson(@RequestParam UUID userId) {
        LessonSummaryItem nextLesson = lessonDashboardService.getNextLesson(userId);
        if (nextLesson == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(nextLesson);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<LessonSummaryItem>> getUpcomingLessons(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "3") Integer limit,
            @RequestParam(defaultValue = "7") Integer windowDays
    ) {
        return ResponseEntity.ok(lessonDashboardService.getUpcomingLessons(userId, limit, windowDays));
    }

    @GetMapping("/tutor/actions")
    public ResponseEntity<TutorDashboardActionsSummary> getTutorActions(@RequestParam UUID userId) {
        return ResponseEntity.ok(lessonDashboardService.getTutorActions(userId));
    }

    @GetMapping("/student/metrics")
    public ResponseEntity<StudentDashboardMetricsSummary> getStudentMetrics(@RequestParam UUID userId) {
        return ResponseEntity.ok(lessonDashboardService.getStudentMetrics(userId));
    }

    @GetMapping("/tutor/today-agenda")
    public ResponseEntity<List<LessonSummaryItem>> getTutorTodayAgenda(
            @RequestParam UUID userId,
            @RequestParam(required = false) String timezone,
            @RequestParam(defaultValue = "5") Integer limit
    ) {
        return ResponseEntity.ok(lessonDashboardService.getTutorTodayAgenda(userId, timezone, limit));
    }
}

package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.TutorStatistics;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final LessonRepository lessonRepository;

    public TutorStatistics getTutorStatistics(UUID tutorId) {
        long count = lessonRepository.countDistinctStudentIdsByTutorIdAndStatus(tutorId, LessonStatus.COMPLETED);
        long completedLessonsThisMonth = countCompletedLessonsThisMonth(tutorId);

        TutorStatistics tutorStatistics = new TutorStatistics();
        tutorStatistics.setTaughtStudents(count);
        tutorStatistics.setCompletedLessons(completedLessonsThisMonth);
        return tutorStatistics;
    }

    private long countCompletedLessonsThisMonth(UUID tutorId) {
        OffsetDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        return lessonRepository.countByTutorIdAndStatusAndDateTimeBetween(tutorId, LessonStatus.COMPLETED, startOfMonth, endOfMonth);
    }
}

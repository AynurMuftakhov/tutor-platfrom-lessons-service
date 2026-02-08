package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.response.LessonSummaryItem;
import com.mytutorplatform.lessonsservice.model.response.StudentDashboardMetricsSummary;
import com.mytutorplatform.lessonsservice.model.response.TutorDashboardActionsSummary;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonDashboardService {

    private static final List<LessonStatus> DASHBOARD_UPCOMING_STATUSES = List.of(
            LessonStatus.SCHEDULED,
            LessonStatus.IN_PROGRESS,
            LessonStatus.RESCHEDULED
    );
    private static final List<LessonStatus> TUTOR_RECENT_ACTIVITY_STATUSES = List.of(
            LessonStatus.COMPLETED,
            LessonStatus.MISSED,
            LessonStatus.IN_PROGRESS,
            LessonStatus.RESCHEDULED,
            LessonStatus.SCHEDULED
    );
    private static final int NOW_CUTOFF_MINUTES = 5;
    private static final int MISSING_NOTES_WINDOW_DAYS = 14;
    private static final int WITHOUT_NEXT_LESSON_WINDOW_DAYS = 30;
    private static final int DEFAULT_TUTOR_AGENDA_LIMIT = 5;
    private static final int DEFAULT_LIMIT = 3;
    private static final int DEFAULT_WINDOW_DAYS = 7;
    private static final int MAX_LIMIT = 20;
    private static final int MAX_WINDOW_DAYS = 30;

    private final LessonRepository lessonRepository;
    private final Clock clock;

    public LessonSummaryItem getNextLesson(UUID userId) {
        Instant nowUtc = Instant.now(clock);
        Instant nowCutoff = nowUtc.minus(NOW_CUTOFF_MINUTES, ChronoUnit.MINUTES);

        List<Lesson> lessons = lessonRepository.findDashboardNextCandidates(
                userId,
                DASHBOARD_UPCOMING_STATUSES,
                nowCutoff.atOffset(ZoneOffset.UTC),
                PageRequest.of(0, 1)
        );

        if (lessons.isEmpty()) {
            logEmptyResult(userId, nowUtc, nowCutoff);
            return null;
        }

        return toSummary(lessons.get(0));
    }

    public List<LessonSummaryItem> getUpcomingLessons(UUID userId, Integer limit, Integer windowDays) {
        int safeLimit = sanitizeLimit(limit);
        int safeWindowDays = sanitizeWindowDays(windowDays);

        Instant nowUtc = Instant.now(clock);
        Instant nowCutoff = nowUtc.minus(NOW_CUTOFF_MINUTES, ChronoUnit.MINUTES);
        Instant windowEndUtc = nowUtc.plus(safeWindowDays, ChronoUnit.DAYS);

        OffsetDateTime fromInclusive = nowCutoff.atOffset(ZoneOffset.UTC);
        OffsetDateTime toInclusive = windowEndUtc.atOffset(ZoneOffset.UTC);

        List<Lesson> lessons = lessonRepository.findDashboardUpcoming(
                userId,
                DASHBOARD_UPCOMING_STATUSES,
                fromInclusive,
                toInclusive,
                PageRequest.of(0, safeLimit)
        );

        if (lessons.isEmpty()) {
            logEmptyResult(userId, nowUtc, nowCutoff);
            return List.of();
        }

        return lessons.stream().map(this::toSummary).toList();
    }

    public TutorDashboardActionsSummary getTutorActions(UUID tutorId) {
        Instant nowUtc = Instant.now(clock);
        Instant nowCutoff = nowUtc.minus(NOW_CUTOFF_MINUTES, ChronoUnit.MINUTES);

        OffsetDateTime nowOffsetUtc = nowUtc.atOffset(ZoneOffset.UTC);
        OffsetDateTime missingNotesFrom = nowUtc.minus(MISSING_NOTES_WINDOW_DAYS, ChronoUnit.DAYS).atOffset(ZoneOffset.UTC);
        OffsetDateTime recentFrom = nowUtc.minus(WITHOUT_NEXT_LESSON_WINDOW_DAYS, ChronoUnit.DAYS).atOffset(ZoneOffset.UTC);
        OffsetDateTime upcomingFrom = nowCutoff.atOffset(ZoneOffset.UTC);

        long missingNotesCount = lessonRepository.countDashboardMissingNotesForTutor(
                tutorId,
                LessonStatus.COMPLETED,
                missingNotesFrom,
                nowOffsetUtc
        );

        long studentsWithoutNextLessonCount = lessonRepository.countDashboardStudentsWithoutNextLesson(
                tutorId,
                TUTOR_RECENT_ACTIVITY_STATUSES,
                recentFrom,
                nowOffsetUtc,
                DASHBOARD_UPCOMING_STATUSES,
                upcomingFrom
        );

        return new TutorDashboardActionsSummary(missingNotesCount, studentsWithoutNextLessonCount);
    }

    public StudentDashboardMetricsSummary getStudentMetrics(UUID studentId) {
        Instant nowUtc = Instant.now(clock);
        YearMonth month = YearMonth.from(nowUtc.atOffset(ZoneOffset.UTC));
        OffsetDateTime fromInclusive = month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toInclusive = month.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.UTC);

        long completedThisMonth = lessonRepository.countDashboardCompletedThisMonthForStudent(
                studentId,
                LessonStatus.COMPLETED,
                fromInclusive,
                toInclusive
        );
        return new StudentDashboardMetricsSummary(completedThisMonth);
    }

    public List<LessonSummaryItem> getTutorTodayAgenda(UUID tutorId, String timezone, Integer limit) {
        ZoneId zone = resolveZone(timezone);
        int safeLimit = sanitizeTutorAgendaLimit(limit);

        LocalDate localToday = Instant.now(clock).atZone(zone).toLocalDate();
        Instant fromUtc = localToday.atStartOfDay(zone).toInstant();
        Instant toUtc = localToday.plusDays(1).atStartOfDay(zone).toInstant();

        List<Lesson> lessons = lessonRepository.findDashboardTutorTodayAgenda(
                tutorId,
                fromUtc.atOffset(ZoneOffset.UTC),
                toUtc.atOffset(ZoneOffset.UTC),
                PageRequest.of(0, safeLimit)
        );

        return lessons.stream().map(this::toSummary).toList();
    }

    private int sanitizeLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(requestedLimit, MAX_LIMIT);
    }

    private int sanitizeTutorAgendaLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return DEFAULT_TUTOR_AGENDA_LIMIT;
        }
        return Math.min(requestedLimit, MAX_LIMIT);
    }

    private ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (DateTimeException ex) {
            return ZoneOffset.UTC;
        }
    }

    private int sanitizeWindowDays(Integer requestedWindowDays) {
        if (requestedWindowDays == null || requestedWindowDays <= 0) {
            return DEFAULT_WINDOW_DAYS;
        }
        return Math.min(requestedWindowDays, MAX_WINDOW_DAYS);
    }

    private LessonSummaryItem toSummary(Lesson lesson) {
        Instant startsAtUtc = lesson.getDateTime().toInstant();
        Instant endsAtUtc = lesson.getEndDate() != null
                ? lesson.getEndDate().toInstant()
                : lesson.getDateTime().plusMinutes(lesson.getDuration()).toInstant();

        return LessonSummaryItem.builder()
                .id(lesson.getId())
                .startsAtUtc(startsAtUtc)
                .endsAtUtc(endsAtUtc)
                .status(lesson.getStatus())
                .title(lesson.getTitle())
                .studentId(lesson.getStudentId())
                .tutorId(lesson.getTutorId())
                .studentName(null)
                .tutorName(null)
                .build();
    }

    private void logEmptyResult(UUID userId, Instant nowUtc, Instant nowCutoff) {
        long totalLessonsCount = lessonRepository.countByStudentIdOrTutorId(userId, userId);
        log.debug(
                "Dashboard lessons empty for userId={}, nowUtc={}, nowCutoff={}, allowedStatuses={}, totalLessonsForUser={}",
                userId,
                nowUtc,
                nowCutoff,
                DASHBOARD_UPCOMING_STATUSES,
                totalLessonsCount
        );
    }
}

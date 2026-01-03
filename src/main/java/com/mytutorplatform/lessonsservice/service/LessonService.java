package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.LessonsMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.RecurringLessonSeries;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import com.mytutorplatform.lessonsservice.model.response.LessonLight;
import com.mytutorplatform.lessonsservice.model.response.LessonBillingFeedItem;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.RecurringLessonSeriesRepository;
import com.mytutorplatform.lessonsservice.repository.specifications.LessonsSpecificationsBuilder;
import com.mytutorplatform.lessonsservice.validation.LessonValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final RecurringLessonSeriesRepository recurringLessonSeriesRepository;
    private final LessonValidator lessonValidator;
    private final LessonsMapper lessonsMapper;
    private final LessonEventService lessonEventService;

    @Transactional
    public Lesson createLesson(CreateLessonRequest createLessonRequest) {
        lessonValidator.validateCreate(createLessonRequest);

        if (Boolean.TRUE.equals(createLessonRequest.getRepeatWeekly())) {
            return createRecurringLessons(createLessonRequest);
        }

        Lesson lesson = lessonsMapper.map(createLessonRequest);
        return lessonRepository.save(lesson);

    }

    private Lesson createRecurringLessons(CreateLessonRequest createLessonRequest) {
        RecurringLessonSeries series = getRecurringLessonSeries(createLessonRequest);

        RecurringLessonSeries savedSeries = recurringLessonSeriesRepository.save(series);

        List<Lesson> lessons = new ArrayList<>();
        OffsetDateTime currentDateTime = createLessonRequest.getDateTime();
        OffsetDateTime endDateTime = savedSeries.getUntil();

        while (!currentDateTime.isAfter(endDateTime)) {
            Lesson lesson = lessonsMapper.map(createLessonRequest);
            lesson.setDateTime(currentDateTime);
            lesson.setSeries(savedSeries);
            lessons.add(lesson);

            currentDateTime = currentDateTime.plusDays(7);
        }

        List<Lesson> savedLessons = lessonRepository.saveAll(lessons);

        return savedLessons.isEmpty() ? null : savedLessons.get(0);
    }

    private RecurringLessonSeries getRecurringLessonSeries(CreateLessonRequest createLessonRequest) {
        RecurringLessonSeries series = new RecurringLessonSeries();
        series.setFrequency(RecurringLessonSeries.RecurrenceFrequency.WEEKLY);
        series.setInterval(1);

        if (createLessonRequest.getRepeatUntil() != null) {
            series.setUntil(createLessonRequest.getRepeatUntil());
        } else if (createLessonRequest.getRepeatWeeksCount() != null) {
            OffsetDateTime endDate = createLessonRequest.getDateTime().plusDays((createLessonRequest.getRepeatWeeksCount() - 1) * 7L);
            series.setUntil(endDate);
        } else {
            OffsetDateTime endDate = createLessonRequest.getDateTime().plusDays(3 * 7);
            series.setUntil(endDate);
        }
        return series;
    }

    public List<Lesson> getAllLessons(UUID tutorId,
                                      UUID studentId,
                                      List<LessonStatus> status,
                                      OffsetDateTime date,
                                      OffsetDateTime startDate,
                                      OffsetDateTime endDate) {
        StartEndDate startEndDate = getStartEndDate(date, startDate, endDate);
        OffsetDateTime startOfDay = startEndDate.startOfDay();
        OffsetDateTime endOfDay = startEndDate.endOfDay();

        Specification<Lesson> lessonsByParamsSpec = LessonsSpecificationsBuilder.lessonsByParams(tutorId, studentId, status, startOfDay, endOfDay, null);

        return lessonRepository.findAll(lessonsByParamsSpec);
    }

    public List<Lesson> getUpcomingLessons(UUID tutorId,
                                           UUID studentId,
                                           List<LessonStatus> status,
                                           OffsetDateTime currentDate,
                                           int limit) {
        Specification<Lesson> params = LessonsSpecificationsBuilder.lessonsByParams(tutorId,
                studentId,
                status,
                currentDate,
                null,
                null);


        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "dateTime"));
        Page<Lesson> page = lessonRepository.findAll(params, pageable);

        return page.getContent();
    }

    public Lesson getCurrentLesson(UUID tutorId, UUID studentId, OffsetDateTime currentDate) {
        List<LessonStatus> statuses = List.of(LessonStatus.SCHEDULED, LessonStatus.IN_PROGRESS, LessonStatus.RESCHEDULED);

        Specification<Lesson> lessonSpecification = LessonsSpecificationsBuilder.currentLessonParams(tutorId, studentId, currentDate, statuses);

        return lessonRepository.findOne(lessonSpecification).orElse(null);
    }

    public Lesson getLessonById(UUID id) {
        return lessonRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
    }

    public Lesson updateLesson(UUID id, UpdateLessonRequest updateLessonRequest) {
        Lesson existingLesson = getLessonById(id);

        lessonValidator.validateUpdate(updateLessonRequest);

        if (existingLesson.getStatus() == LessonStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed lesson");
        }

/*
        if (updateLessonRequest.getDateTime() != null && existingLesson.getDateTime() != updateLessonRequest.getDateTime()) {
            lessonEventService.handleLessonRescheduled(existingLesson, updateLessonRequest.getDateTime());
        }
*/

        lessonsMapper.update(existingLesson, updateLessonRequest);

        return lessonRepository.save(existingLesson);
    }

    @Transactional
    public void deleteLesson(UUID id, boolean deleteSeries) {
        Lesson lesson = getLessonById(id);

        if (!deleteSeries) {
            lessonRepository.deleteById(id);
            return;
        }

        RecurringLessonSeries series = lesson.getSeries();
        if (series == null) {
            lessonRepository.deleteById(id);
            return;
        }

        // Delete only future lessons in the series (keep past lessons regardless of status)
        List<Lesson> seriesLessons = lessonRepository.findAllBySeries(series);
        OffsetDateTime now = OffsetDateTime.now();
        List<Lesson> futureLessons = seriesLessons.stream()
                .filter(l -> l.getDateTime() != null && l.getDateTime().isAfter(now))
                .toList();

        if (!futureLessons.isEmpty()) {
            lessonRepository.deleteAll(futureLessons);
        }

        // If no lessons remain in the series after deletion, remove the series record
        if (futureLessons.size() == seriesLessons.size()) {
            recurringLessonSeriesRepository.delete(series);
        }
    }

    private static StartEndDate getStartEndDate(OffsetDateTime date, OffsetDateTime startTime, OffsetDateTime endTime) {
        if (startTime != null && endTime != null) {
            return new StartEndDate(startTime, endTime);
        }

        if (date == null) {
            return new StartEndDate(null, null);
        }

        ZoneOffset offset = date.getOffset();
        LocalDate localDate = date.toLocalDate();

        OffsetDateTime startOfDay = localDate.atStartOfDay().atOffset(offset);
        OffsetDateTime endOfDay = localDate.plusDays(1).atStartOfDay().minusNanos(1).atOffset(offset);

        return new StartEndDate(startOfDay, endOfDay);
    }

    public List<LessonLight> getMyTutorSchedule(UUID tutorId, UUID studentId, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<LessonStatus> status = List.of(LessonStatus.SCHEDULED, LessonStatus.IN_PROGRESS, LessonStatus.RESCHEDULED, LessonStatus.COMPLETED);

        Specification<Lesson> lessonsByParamsSpec = LessonsSpecificationsBuilder.lessonsByParams(tutorId, studentId, status, startDate, endDate, null);

        List<Lesson> lessons = lessonRepository.findAll(lessonsByParamsSpec);
        return lessonsMapper.mapListWithoutSensitiveFields(lessons);
    }

    private record StartEndDate(OffsetDateTime startOfDay, OffsetDateTime endOfDay) {}

    public Map<String, Integer> getLessonCountsByMonth(int year, int month, UUID studentId, UUID tutorId) {
        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

        OffsetDateTime startDateTime = firstDayOfMonth.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = lastDayOfMonth.plusDays(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.UTC);

        Specification<Lesson> spec = LessonsSpecificationsBuilder.lessonsByParams(tutorId, studentId, null, startDateTime, endDateTime, null);

        List<Lesson> lessons = lessonRepository.findAll(spec);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Integer> countsByDay = lessons.stream()
            .collect(Collectors.groupingBy(
                lesson -> lesson.getDateTime().toLocalDate().format(formatter),
                Collectors.summingInt(lesson -> 1)
            ));

        Map<String, Integer> result = new HashMap<>();
        for (int day = 1; day <= lastDayOfMonth.getDayOfMonth(); day++) {
            String dateKey = LocalDate.of(year, month, day).format(formatter);
            result.put(dateKey, countsByDay.getOrDefault(dateKey, 0));
        }

        return result;
    }

    public List<LessonBillingFeedItem> getBillingFeed(UUID tutorId, LocalDate fromDate, LocalDate toDate, List<LessonStatus> statuses) {
        OffsetDateTime from = fromDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.UTC);

        Specification<Lesson> spec = LessonsSpecificationsBuilder.lessonsByParams(
                tutorId,
                null,
                statuses,
                from,
                to,
                null
        );
        List<Lesson> lessons = lessonRepository.findAll(spec);
        return lessons.stream().map(lesson -> LessonBillingFeedItem.builder()
                .lessonId(lesson.getId())
                .tutorId(lesson.getTutorId())
                .studentId(lesson.getStudentId())
                .status(lesson.getStatus())
                .startTime(lesson.getDateTime())
                .durationMinutes(lesson.getDuration())
                .completedAt(lesson.getUpdatedAt() != null ? lesson.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .priceOverride(null)
                .quantity(java.math.BigDecimal.ONE)
                .currency(null)
                .build()).toList();
    }
}

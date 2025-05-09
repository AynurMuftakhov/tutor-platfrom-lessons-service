package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.kafka.producer.LessonEventProducer;
import com.mytutorplatform.lessonsservice.mapper.LessonsMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.RecurringLessonSeries;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.RecurringLessonSeriesRepository;
import com.mytutorplatform.lessonsservice.repository.specifications.LessonsSpecificationsBuilder;
import com.mytutorplatform.lessonsservice.validation.LessonValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

    public Page<Lesson> getAllLessons(UUID tutorId, UUID studentId, List<LessonStatus> status, String date, Pageable pageable) {
        StartEndDate startEndDate = getStartEndDate(date);
        OffsetDateTime startOfDay = startEndDate.startOfDay();
        OffsetDateTime endOfDay = startEndDate.endOfDay();


        Specification<Lesson> lessonsByParamsSpec = LessonsSpecificationsBuilder.lessonsByParams(tutorId, studentId, status, startOfDay, endOfDay);

        return lessonRepository.findAll(lessonsByParamsSpec, pageable);
    }

    public Lesson getLessonById(UUID id) {
        return lessonRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
    }

    public Lesson updateLesson(UUID id, UpdateLessonRequest updateLessonRequest) {
        Lesson existingLesson = getLessonById(id);

        lessonValidator.validateUpdate(existingLesson, updateLessonRequest);

        if (existingLesson.getStatus() == LessonStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed lesson");
        }

        if (existingLesson.getDateTime() != updateLessonRequest.getDateTime()) {
            lessonEventService.handleLessonRescheduled(existingLesson, updateLessonRequest.getDateTime());
        }

        lessonsMapper.update(existingLesson, updateLessonRequest);

        return lessonRepository.save(existingLesson);
    }

    public void deleteLesson(UUID id) {
        Lesson lesson = getLessonById(id);

        if (lesson.getStatus() == LessonStatus.COMPLETED) {
            throw new IllegalStateException("Cannot delete a completed lesson");
        }

        lessonRepository.deleteById(id);
    }

    private static StartEndDate getStartEndDate(String date) {
        if (date == null || date.isEmpty()) {
            return new StartEndDate(null, null);
        }

        LocalDate localDate = LocalDate.parse(date);

        OffsetDateTime startOfDay = localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = localDate.plusDays(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.UTC);
        return new StartEndDate(startOfDay, endOfDay);
    }

    private record StartEndDate(OffsetDateTime startOfDay, OffsetDateTime endOfDay) {}

    public Map<String, Integer> getLessonCountsByMonth(int year, int month, UUID studentId, UUID tutorId) {
        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

        OffsetDateTime startDateTime = firstDayOfMonth.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = lastDayOfMonth.plusDays(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.UTC);

        Specification<Lesson> spec = LessonsSpecificationsBuilder.lessonsByParams(tutorId, studentId, null, startDateTime, endDateTime);

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
}

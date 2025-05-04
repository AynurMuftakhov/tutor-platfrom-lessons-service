package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.kafka.producer.LessonEventProducer;
import com.mytutorplatform.lessonsservice.mapper.LessonsMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.specifications.LessonsSpecificationsBuilder;
import com.mytutorplatform.lessonsservice.validation.LessonValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonValidator lessonValidator;
    private final LessonsMapper lessonsMapper;
    private final LessonEventService lessonEventService;

    public Lesson createLesson(CreateLessonRequest createLessonRequest) {
        lessonValidator.validateCreate(createLessonRequest);

        Lesson lesson = lessonsMapper.map(createLessonRequest);

        return lessonRepository.save(lesson);
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
}

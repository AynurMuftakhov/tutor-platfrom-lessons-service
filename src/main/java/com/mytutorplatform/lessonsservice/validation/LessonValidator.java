package com.mytutorplatform.lessonsservice.validation;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.LessonsRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonValidator {
    private final LessonRepository lessonRepository;

    public void validateCreate(CreateLessonRequest createLessonRequest) {
        validateCommonFields(createLessonRequest);

        // Validate recurring lesson fields if applicable
        if (Boolean.TRUE.equals(createLessonRequest.getRepeatWeekly())) {
            validateRecurringLessonFields(createLessonRequest);
        }

        validateConflicts(createLessonRequest.getTutorId(), null, createLessonRequest);
    }

    private void validateRecurringLessonFields(CreateLessonRequest createLessonRequest) {
        // If repeatWeekly is true, at least one of repeatWeeksCount or repeatUntil should be provided
        if (createLessonRequest.getRepeatWeeksCount() == null && createLessonRequest.getRepeatUntil() == null) {
            throw new IllegalArgumentException("Either repeatWeeksCount or repeatUntil must be provided for recurring lessons");
        }

        // Validate repeatWeeksCount if provided
        if (createLessonRequest.getRepeatWeeksCount() != null && createLessonRequest.getRepeatWeeksCount() <= 0) {
            throw new IllegalArgumentException("repeatWeeksCount must be greater than 0");
        }

        // Validate repeatUntil if provided
        if (createLessonRequest.getRepeatUntil() != null && createLessonRequest.getRepeatUntil().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("repeatUntil must be in the future");
        }
    }

    public void validateUpdate(Lesson existingLesson, UpdateLessonRequest lessonRequest){
        validateCommonFields(lessonRequest);

        validateConflicts(existingLesson.getTutorId(), existingLesson.getId(), lessonRequest);
    }

    private void validateCommonFields(LessonsRequest createLessonRequest) {
        if (createLessonRequest.getDateTime() != null && createLessonRequest.getDateTime().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Lesson date and time must be in the future");
        }

        if (createLessonRequest.getDuration() != null && createLessonRequest.getDuration() <= 0) {
            throw new IllegalArgumentException("Lesson duration must be greater than 0");
        }
    }

    private void validateConflicts(UUID tutorId, UUID lessonId, LessonsRequest lessonsRequest) {
        if (lessonsRequest.getDuration() == null || lessonsRequest.getDateTime() == null) {
            return;
        }

        List<Lesson> conflictingLessons = lessonRepository.findLessonsByTutorAndDateRange(
                        tutorId,
                        lessonsRequest.getDateTime().minusMinutes(lessonsRequest.getDuration()),
                        lessonsRequest.getDateTime().plusMinutes(lessonsRequest.getDuration())
                ).stream()
                .filter(existing -> !existing.getId().equals(lessonId))
                .toList();

        if (!conflictingLessons.isEmpty()) {
            throw new IllegalArgumentException("The tutor has conflicting lessons");
        }
    }
}

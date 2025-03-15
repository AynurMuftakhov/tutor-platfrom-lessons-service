package com.mytutorplatform.lessonsservice.validation;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonValidator {
    @Autowired
    private LessonRepository lessonRepository;

    public void validateLesson(Lesson lesson) {
        if (lesson.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lesson date and time must be in the future");
        }

        List<Lesson> conflictingLessons = lessonRepository.findLessonsByTutorAndDateRange(
                lesson.getTutorId(),
                lesson.getDateTime().minusMinutes(lesson.getDuration()),
                lesson.getDateTime().plusMinutes(lesson.getDuration())
        );

        if (!conflictingLessons.isEmpty()) {
            throw new IllegalArgumentException("The tutor has conflicting lessons");
        }
    }
}

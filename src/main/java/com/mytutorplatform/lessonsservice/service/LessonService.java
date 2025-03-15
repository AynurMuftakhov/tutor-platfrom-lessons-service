package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.validation.LessonValidator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonValidator lessonValidator;

    public Lesson createLesson(Lesson lesson) {
        lessonValidator.validateLesson(lesson);

        return lessonRepository.save(lesson);
    }

    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    public Lesson getLessonById(UUID id) {
        return lessonRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
    }

    public Lesson updateLesson(UUID id, Lesson updatedLesson) {
        lessonValidator.validateLesson(updatedLesson);

        Lesson existingLesson = getLessonById(id);

        if (existingLesson.getStatus() == LessonStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed lesson");
        }

        existingLesson.setTitle(updatedLesson.getTitle());
        existingLesson.setDateTime(updatedLesson.getDateTime());
        existingLesson.setStatus(updatedLesson.getStatus());
        existingLesson.setPrice(updatedLesson.getPrice());
        return lessonRepository.save(existingLesson);
    }

    public void deleteLesson(UUID id) {
        Lesson lesson = getLessonById(id);

        if (lesson.getStatus() == LessonStatus.COMPLETED) {
            throw new IllegalStateException("Cannot delete a completed lesson");
        }

        lessonRepository.deleteById(id);
    }


}

package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.LessonsMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.validation.LessonValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonValidator lessonValidator;
    private final LessonsMapper lessonsMapper;

    public Lesson createLesson(CreateLessonRequest createLessonRequest) {
        lessonValidator.validateCreate(createLessonRequest);

        Lesson lesson = lessonsMapper.map(createLessonRequest);

        return lessonRepository.save(lesson);
    }

    public Page<Lesson> getAllLessons(UUID tutorId, UUID studentId, List<LessonStatus> status, Pageable pageable) {
        if (studentId != null) {
            return lessonRepository.findLessonsByStudentAndStatus(studentId, status, pageable);
        }

        return lessonRepository.findLessonsByTutorIdAndStatus(tutorId, status, pageable);
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


}

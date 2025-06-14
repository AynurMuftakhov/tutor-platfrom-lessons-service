package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonTaskService {

    private final LessonRepository lessonRepository;

    @Transactional
    public void assignTaskToLesson(UUID lessonId, UUID taskId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));

        List<UUID> taskIds = lesson.getTaskIds();
        if (taskIds == null) {
            taskIds = List.of(taskId);
        } else {
            taskIds.add(taskId);
        }

        lesson.setTaskIds(taskIds);
        lessonRepository.save(lesson);
    }

    public void deleteTaskFromLesson(UUID lessonId, UUID taskId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));

        List<UUID> taskIds = lesson.getTaskIds();
        if (taskIds == null) {
            return;
        }

        taskIds.remove(taskId);

        lessonRepository.save(lesson);
    }
}

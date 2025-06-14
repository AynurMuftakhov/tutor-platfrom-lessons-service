package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.ListeningTaskMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.ListeningTaskRepository;
import com.mytutorplatform.lessonsservice.validation.ListeningTaskValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListeningTaskService {

    private final ListeningTaskRepository listeningTaskRepository;
    private final LessonRepository lessonRepository;
    private final ListeningTaskValidator listeningTaskValidator;
    private final ListeningTaskMapper listeningTaskMapper;

    @Transactional
    public ListeningTask createListeningTask(CreateListeningTaskRequest request) {
        listeningTaskValidator.validateCreate(request);

        ListeningTask listeningTask = listeningTaskMapper.map(request);

        return listeningTaskRepository.save(listeningTask);
    }

    public List<ListeningTask> getListeningTasksByLessonId(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));

        if (CollectionUtils.isEmpty(lesson.getTaskIds())) {
            return Collections.emptyList();
        }

        return listeningTaskRepository.findAllById(lesson.getTaskIds());
    }

    @Transactional
    public void deleteListeningTask(UUID taskId) {
        if (!listeningTaskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Listening task not found with id: " + taskId);
        }
        
        listeningTaskRepository.deleteById(taskId);
    }

    @Transactional
    public void deleteListeningTaskFromLesson(UUID lessonId, UUID taskId) {
        if (!listeningTaskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Listening task not found with id: " + taskId);
        }

        listeningTaskRepository.deleteById(taskId);
    }

    public List<ListeningTask> getAllListeningTasks() {
        return listeningTaskRepository.findAll();
    }
}
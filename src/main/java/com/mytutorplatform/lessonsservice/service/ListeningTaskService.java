package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.ListeningTaskRepository;
import com.mytutorplatform.lessonsservice.validation.ListeningTaskValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListeningTaskService {

    private final ListeningTaskRepository listeningTaskRepository;
    private final LessonRepository lessonRepository;
    private final ListeningTaskValidator listeningTaskValidator;

    @Transactional
    public ListeningTask createListeningTask(UUID lessonId, CreateListeningTaskRequest request) {
        listeningTaskValidator.validateCreate(request);
        
        if (!lessonRepository.existsById(lessonId)) {
            throw new EntityNotFoundException("Lesson not found with id: " + lessonId);
        }
        
        ListeningTask listeningTask = new ListeningTask();
        listeningTask.setLessonId(lessonId);
        listeningTask.setAssetType(request.getAssetType());
        listeningTask.setSourceUrl(request.getSourceUrl());
        listeningTask.setStartSec(request.getStartSec());
        listeningTask.setEndSec(request.getEndSec());
        listeningTask.setWordLimit(request.getWordLimit());
        listeningTask.setTimeLimitSec(request.getTimeLimitSec());
        
        return listeningTaskRepository.save(listeningTask);
    }

    public List<ListeningTask> getListeningTasksByLessonId(UUID lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new EntityNotFoundException("Lesson not found with id: " + lessonId);
        }
        
        return listeningTaskRepository.findByLessonId(lessonId);
    }

    @Transactional
    public void deleteListeningTask(UUID taskId) {
        if (!listeningTaskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Listening task not found with id: " + taskId);
        }
        
        listeningTaskRepository.deleteById(taskId);
    }
}
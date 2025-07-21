package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.ListeningTaskMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.ListeningTaskRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
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
    private final MaterialRepository materialRepository;
    private final ListeningTaskValidator listeningTaskValidator;
    private final ListeningTaskMapper listeningTaskMapper;

    @Transactional
    public ListeningTask createListeningTask(CreateListeningTaskRequest request) {
        listeningTaskValidator.validateCreate(request);

        // Verify that the material exists
        if (request.getMaterialId() != null) {
            materialRepository.findById(request.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException("Material not found with id: " + request.getMaterialId()));
        }

        ListeningTask listeningTask = listeningTaskMapper.map(request);

        return listeningTaskRepository.save(listeningTask);
    }

    @Transactional
    public ListeningTask updateListeningTask(UUID taskId, CreateListeningTaskRequest request) {
        ListeningTask existingTask = listeningTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Listening task not found with id: " + taskId));

        // Verify that the material exists if it's being changed
        if (request.getMaterialId() != null && !request.getMaterialId().equals(existingTask.getMaterialId())) {
            materialRepository.findById(request.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException("Material not found with id: " + request.getMaterialId()));
        }

        if (request.getTitle() != null) {
            existingTask.setTitle(request.getTitle());
        }
        if (request.getStartSec() != null) {
            existingTask.setStartSec(request.getStartSec());
        }
        if (request.getEndSec() != null) {
            existingTask.setEndSec(request.getEndSec());
        }
        if (request.getWordLimit() != null) {
            existingTask.setWordLimit(request.getWordLimit());
        }
        if (request.getTimeLimitSec() != null) {
            existingTask.setTimeLimitSec(request.getTimeLimitSec());
        }
        if (request.getMaterialId() != null) {
            existingTask.setMaterialId(request.getMaterialId());
        }

        return listeningTaskRepository.save(existingTask);
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

    public List<ListeningTask> getAllListeningTasks(UUID materialId) {
        if (materialId == null) {
            return listeningTaskRepository.findAll();
        }
       return listeningTaskRepository.findByMaterialId(materialId);
    }
}

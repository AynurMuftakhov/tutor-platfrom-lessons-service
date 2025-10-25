package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.ListeningTaskMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.ListeningTaskStatus;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.model.request.ListeningVoiceConfigRequest;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.ListeningTaskRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import com.mytutorplatform.lessonsservice.validation.ListeningTaskValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
    private final ListeningAudioJobService audioJobService;

    @Transactional
    public ListeningTask createListeningTask(CreateListeningTaskRequest request) {
        listeningTaskValidator.validateCreate(request);

        // Verify that the material exists
        if (request.getMaterialId() != null) {
            materialRepository.findById(request.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException("Material not found with id: " + request.getMaterialId()));
        }

        ListeningTask listeningTask = listeningTaskMapper.map(request);
        applyDefaults(listeningTask);

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

        listeningTaskMapper.update(existingTask, request);

        // If audioUrl is provided (not null) and status not provided in request, auto-transition to READY
        if (request.getAudioUrl() != null && request.getStatus() == null) {
            if (request.getAudioUrl().isBlank()) {
                // if blank provided, keep current status
            } else {
                existingTask.setStatus(ListeningTaskStatus.READY);
            }
        }

        applyDefaults(existingTask);

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

    public ListeningTask updateTaskAudioWithJob(UUID materialId, UUID taskId, UUID jobId) {
        ListeningTask task = listeningTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Listening task not found with id: " + taskId));
        if (task.getMaterialId() != null && materialId != null && !task.getMaterialId().equals(materialId)) {
            throw new EntityNotFoundException("Task does not belong to material: " + materialId);
        }
        var jobOpt = audioJobService.getJob(jobId);
        var job = jobOpt.orElseThrow(() -> new EntityNotFoundException("Audio job not found with id: " + jobId));
        if (job.getAudioUrl() != null && !job.getAudioUrl().isBlank()) {
            task.setAudioUrl(job.getAudioUrl());
            task.setStatus(ListeningTaskStatus.READY);
        }
        if (job.getLanguageCode() != null) {
            task.setLanguage(job.getLanguageCode());
        }
        return listeningTaskRepository.save(task);
    }

    public ListeningTask updateTaskAudioDirect(UUID materialId, UUID taskId, String audioUrl, ListeningVoiceConfigRequest voiceReq, String language) {
        ListeningTask task = listeningTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Listening task not found with id: " + taskId));
        if (task.getMaterialId() != null && materialId != null && !task.getMaterialId().equals(materialId)) {
            throw new EntityNotFoundException("Task does not belong to material: " + materialId);
        }
        if (audioUrl != null) {
            task.setAudioUrl(audioUrl);
            if (!audioUrl.isBlank()) {
                task.setStatus(ListeningTaskStatus.READY);
            }
        }
        if (language != null) {
            task.setLanguage(language);
        }

        return listeningTaskRepository.save(task);
    }

    private void applyDefaults(ListeningTask task) {
        if (task.getTargetWords() == null) {
            task.setTargetWords(new ArrayList<>());
        }
        if (task.getStatus() == null) {
            task.setStatus(ListeningTaskStatus.DRAFT);
        }
    }
}

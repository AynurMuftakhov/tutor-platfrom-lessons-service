package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.service.ListeningTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ListeningTaskController {

    private final ListeningTaskService listeningTaskService;

    @PostMapping("/lessons/{id}/listening-tasks")
    public ResponseEntity<ListeningTask> createListeningTask(
            @PathVariable("id") UUID lessonId,
            @RequestBody CreateListeningTaskRequest request) {
        ListeningTask createdTask = listeningTaskService.createListeningTask(lessonId, request);
        return ResponseEntity.ok(createdTask);
    }

    @GetMapping("/lessons/{id}/listening-tasks")
    public ResponseEntity<List<ListeningTask>> getListeningTasksByLessonId(
            @PathVariable("id") UUID lessonId) {
        List<ListeningTask> tasks = listeningTaskService.getListeningTasksByLessonId(lessonId);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/listening-tasks/{taskId}")
    public ResponseEntity<Void> deleteListeningTask(
            @PathVariable UUID taskId) {
        listeningTaskService.deleteListeningTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.service.LessonTaskService;
import com.mytutorplatform.lessonsservice.service.ListeningTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LessonTaskController {

    private final ListeningTaskService listeningTaskService;
    private final LessonTaskService lessonTaskService;

    @PostMapping("/lessons/{id}/tasks")
    public ResponseEntity<ListeningTask> assignTaskToLesson(
            @PathVariable("id") UUID lessonId,
            @RequestParam UUID taskId) {
        lessonTaskService.assignTaskToLesson(lessonId, taskId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/lessons/{id}/tasks")
    public ResponseEntity<List<ListeningTask>> getListeningTasksByLessonId(
            @PathVariable("id") UUID lessonId) {
        List<ListeningTask> tasks = listeningTaskService.getListeningTasksByLessonId(lessonId);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("lessons/{id}/tasks/{taskId}")
    public ResponseEntity<Void> deleteListeningTasksByLessonId(@PathVariable UUID id,
                                                               @PathVariable UUID taskId){
        lessonTaskService.deleteTaskFromLesson(id, taskId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/listening-tasks/{taskId}")
    public ResponseEntity<Void> deleteListeningTask(
            @PathVariable UUID taskId) {
        listeningTaskService.deleteListeningTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listening-tasks")
    public List<ListeningTask> getAllListeningTasks() {
        return listeningTaskService.getAllListeningTasks();
    }

    @PostMapping("/listening-tasks")
    public ResponseEntity<ListeningTask> createListeningTask(@RequestBody CreateListeningTaskRequest request) {
        ListeningTask createdTask = listeningTaskService.createListeningTask(request);
        return ResponseEntity.ok(createdTask);
    }
}
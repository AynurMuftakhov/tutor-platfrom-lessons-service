package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.mapper.ListeningTaskMapper;
import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.model.request.CreateGrammarItemRequest;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.model.request.GrammarScoreRequest;
import com.mytutorplatform.lessonsservice.model.request.ListeningTaskAudioUpdateRequest;
import com.mytutorplatform.lessonsservice.model.response.GrammarItemDto;
import com.mytutorplatform.lessonsservice.model.response.GrammarScoreResponse;
import com.mytutorplatform.lessonsservice.model.response.ListeningTaskDTO;
import com.mytutorplatform.lessonsservice.service.GrammarItemService;
import com.mytutorplatform.lessonsservice.service.GrammarScoringService;
import com.mytutorplatform.lessonsservice.service.ListeningAudioJobService;
import com.mytutorplatform.lessonsservice.service.ListeningTaskService;
import com.mytutorplatform.lessonsservice.service.MaterialService;
import com.mytutorplatform.lessonsservice.repository.ListeningTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService service;
    private final ListeningTaskService listeningTaskService;
    private final GrammarItemService grammarItemService;
    private final GrammarScoringService grammarScoringService;
    private final ListeningTaskMapper listeningTaskMapper;

    @GetMapping
    public Page<Material> getMaterials(
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) List<String> tags
    ) {
        return service.findMaterials(folderId, search, type, tags, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable UUID id) {
        Material material = service.getMaterialById(id);
        return ResponseEntity.ok(material);
    }

    @PostMapping
    public ResponseEntity<Material> createMaterial(@RequestBody Material material) {
        Material createdMaterial = service.createMaterial(material);
        return ResponseEntity.ok(createdMaterial);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Material> updateMaterial(@PathVariable UUID id, @RequestBody Material material) {
        Material updatedMaterial = service.updateMaterial(id, material);
        return ResponseEntity.ok(updatedMaterial);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID id) {
        service.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<ListeningTaskDTO>> getTasksForMaterial(@PathVariable UUID id) {
        List<ListeningTask> tasks = service.getTasksForMaterial(id);
        return ResponseEntity.ok(listeningTaskMapper.mapList(tasks));
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<ListeningTaskDTO> createTaskForMaterial(
            @PathVariable UUID id,
            @RequestBody CreateListeningTaskRequest request) {
        request.setMaterialId(id);
        ListeningTask createdTask = listeningTaskService.createListeningTask(request);
        return ResponseEntity.ok(listeningTaskMapper.map(createdTask));
    }

    @PatchMapping("/{materialId}/tasks/{taskId}")
    public ResponseEntity<ListeningTaskDTO> updateTaskForMaterial(
            @PathVariable UUID materialId,
            @PathVariable UUID taskId,
            @RequestBody CreateListeningTaskRequest request) {
        request.setMaterialId(materialId);
        ListeningTask updatedTask = listeningTaskService.updateListeningTask(taskId, request);
        return ResponseEntity.ok(listeningTaskMapper.map(updatedTask));
    }

    @DeleteMapping("/{materialId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTaskForMaterial(
            @PathVariable UUID materialId,
            @PathVariable UUID taskId) {
        listeningTaskService.deleteListeningTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // Optional: Attach audio to a listening task by jobId or direct URL + voice
    @PostMapping("/{materialId}/tasks/{taskId}/audio")
    public ResponseEntity<ListeningTaskDTO> updateTaskAudio(
            @PathVariable UUID materialId,
            @PathVariable UUID taskId,
            @RequestBody ListeningTaskAudioUpdateRequest request) {
        ListeningTask updated;
        if (request.getJobId() != null) {
            updated = listeningTaskService.updateTaskAudioWithJob(materialId, taskId, request.getJobId());
        } else {
            updated = listeningTaskService.updateTaskAudioDirect(materialId, taskId, request.getAudioUrl(), request.getVoice(), request.getLanguage());
        }
        return ResponseEntity.ok(listeningTaskMapper.map(updated));
    }

    @PostMapping("/{id}/grammar-items")
    public GrammarItemDto createGrammarItemForMaterial(@PathVariable UUID id, @RequestBody CreateGrammarItemRequest grammarItem) {
        return grammarItemService.createItem(id, grammarItem);
    }

    @GetMapping("/{id}/grammar-items")
    public List<GrammarItemDto> getGrammarItemsForMaterial(@PathVariable UUID id) {
        return grammarItemService.getItemsByMaterialId(id);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = service.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * Scores a student's attempts at grammar items in a material.
     *
     * @param materialId The ID of the material containing the grammar items
     * @param request The request containing the student's attempts
     * @return A detailed score report
     */
    @PostMapping("/{materialId}/score")
    public ResponseEntity<GrammarScoreResponse> scoreGrammarItems(
            @PathVariable UUID materialId,
            @RequestBody GrammarScoreRequest request) {
        GrammarScoreResponse response = grammarScoringService.score(materialId, request.getAttempts());
        return ResponseEntity.ok(response);
    }
}

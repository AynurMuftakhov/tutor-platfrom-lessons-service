package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.mapper.LessonContentMapper;
import com.mytutorplatform.lessonsservice.model.LessonContent;
import com.mytutorplatform.lessonsservice.model.LessonContentStatus;
import com.mytutorplatform.lessonsservice.model.response.LessonContentDto;
import com.mytutorplatform.lessonsservice.service.LessonContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lesson-contents")
@RequiredArgsConstructor
public class LessonContentController {

    private final LessonContentService service;
    private final LessonContentMapper mapper;

    @PostMapping
    public ResponseEntity<LessonContentDto> create(@RequestBody LessonContentDto dto) {
        // ownerId is required by spec
        LessonContent toCreate = mapper.toEntity(dto);
        LessonContent created = service.create(toCreate);
        return ResponseEntity.ok(mapper.toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonContentDto> get(@PathVariable UUID id) {
        LessonContent lc = service.getById(id);
        return ResponseEntity.ok(mapper.toDto(lc));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonContentDto> update(@PathVariable UUID id, @RequestBody LessonContentDto dto) {
        LessonContent patch = mapper.toEntity(dto);
        LessonContent updated = service.update(id, patch);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<LessonContentDto> publish(@PathVariable UUID id) {
        LessonContent updated = service.publish(id);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<LessonContentDto> unpublish(@PathVariable UUID id) {
        LessonContent updated = service.unpublish(id);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Page<LessonContentDto> list(@RequestParam UUID ownerId,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false) String tags,
                                       @RequestParam(required = false) LessonContentStatus status,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        List<String> tagList = null;
        if (tags != null && !tags.isBlank()) {
            tagList = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        Page<LessonContent> res = service.list(ownerId, q, tagList, status, page, size);
        return res.map(mapper::toDto);
    }
}

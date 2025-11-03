package com.mytutorplatform.lessonsservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mytutorplatform.lessonsservice.model.LessonContent;
import com.mytutorplatform.lessonsservice.model.LessonContentStatus;
import com.mytutorplatform.lessonsservice.model.response.LessonContentDto;
import com.mytutorplatform.lessonsservice.repository.LessonContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonContentService {

    private final LessonContentRepository repository;

    @Transactional(readOnly = true)
    public LessonContent getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LessonContent not found"));
    }

    public LessonContent create(LessonContent input) {
        validate(input.getLayout(), input.getContent());
        input.setId(null);
        input.setStatus(LessonContentStatus.DRAFT);
        if (input.getCreatedBy() == null){
            input.setCreatedBy(input.getOwnerId());
        }
        return repository.save(input);
    }

    public LessonContent update(UUID id, LessonContent patch) {
        LessonContent existing = getById(id);
        // allow updates for title, tags, coverImageUrl, layout, content
        if (patch.getTitle() != null) existing.setTitle(patch.getTitle());
        if (patch.getTags() != null) existing.setTags(patch.getTags());
        if (patch.getCoverImageUrl() != null) existing.setCoverImageUrl(patch.getCoverImageUrl());
        if (patch.getLayout() != null) existing.setLayout(patch.getLayout());
        if (patch.getContent() != null) existing.setContent(patch.getContent());
        validate(existing.getLayout(), existing.getContent());
        return repository.save(existing);
    }

    public LessonContent publish(UUID id) {
        LessonContent existing = getById(id);
        existing.setStatus(LessonContentStatus.PUBLISHED);
        return repository.save(existing);
    }

    public LessonContent unpublish(UUID id) {
        LessonContent existing = getById(id);
        existing.setStatus(LessonContentStatus.DRAFT);
        return repository.save(existing);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<LessonContent> list(UUID ownerId,
                                    String q,
                                    List<String> tags,
                                    LessonContentStatus status,
                                    int page,
                                    int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LessonContent> basePage;
        if (q != null && !q.isBlank()) {
            basePage = repository.searchByOwnerIdAndTitleIlike(ownerId, q, pageable);
        } else {
            basePage = repository.findAllByOwnerId(ownerId, pageable);
        }
        // In-memory filter for tags and status (simplified to satisfy current requirements)
        List<LessonContent> filtered = new ArrayList<>(basePage.getContent());
        if (status != null) {
            filtered = filtered.stream().filter(lc -> lc.getStatus() == status).collect(Collectors.toList());
        }
        if (tags != null && !tags.isEmpty()) {
            filtered = filtered.stream().filter(lc -> {
                String[] entityTags = lc.getTags();
                if (entityTags == null || entityTags.length == 0) return false;
                // require that all provided tags are present
                for (String t : tags) {
                    boolean present = false;
                    for (String et : entityTags) {
                        if (Objects.equals(et, t)) { present = true; break; }
                    }
                    if (!present) return false;
                }
                return true;
            }).collect(Collectors.toList());
        }

        return new PageImpl<>(filtered, pageable, basePage.getTotalElements());
    }

    private void validate(JsonNode layout, JsonNode content) {
        if (layout == null || content == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "layout and content must be provided");
        }
        if (!layout.isObject() || !content.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "layout and content must be JSON objects");
        }
    }
}

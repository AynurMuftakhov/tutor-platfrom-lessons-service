package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import com.mytutorplatform.lessonsservice.repository.specifications.MaterialSpecificationsBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterialService {

    private final MaterialRepository repository;

    @Transactional(readOnly = true)
    public Page<Material> findMaterials(UUID folderId, String search, String type, List<String> tags, int page, int size) {
        Specification<Material> spec = new MaterialSpecificationsBuilder()
                .withFolderId(folderId)
                .withSearch(search)
                .withType(type)
                .withTags(tags)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(spec, pageable);
    }
}
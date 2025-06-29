package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.repository.ListeningTaskRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import com.mytutorplatform.lessonsservice.repository.specifications.MaterialSpecificationsBuilder;
import jakarta.persistence.EntityNotFoundException;
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
    private final ListeningTaskRepository listeningTaskRepository;

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

    @Transactional(readOnly = true)
    public Material getMaterialById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with id: " + id));
    }

    @Transactional
    public Material createMaterial(Material material) {
        return repository.save(material);
    }

    @Transactional
    public Material updateMaterial(UUID id, Material material) {
        Material existingMaterial = getMaterialById(id);

        if (material.getTitle() != null) {
            existingMaterial.setTitle(material.getTitle());
        }
        if (material.getType() != null) {
            existingMaterial.setType(material.getType());
        }
        if (material.getSourceUrl() != null) {
            existingMaterial.setSourceUrl(material.getSourceUrl());
        }
        if (material.getThumbnailUrl() != null) {
            existingMaterial.setThumbnailUrl(material.getThumbnailUrl());
        }
        if (material.getDurationSec() != null) {
            existingMaterial.setDurationSec(material.getDurationSec());
        }
        if (material.getTags() != null) {
            existingMaterial.setTags(material.getTags());
        }

        if (material.getFolderId() != null) {
            existingMaterial.setFolderId(material.getFolderId());
        }

        return repository.save(existingMaterial);
    }

    @Transactional
    public void deleteMaterial(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Material not found with id: " + id);
        }

        // Delete all listening tasks associated with this material
        List<ListeningTask> tasks = listeningTaskRepository.findByMaterialId(id);
        listeningTaskRepository.deleteAll(tasks);

        repository.deleteById(id);
    }

    @Transactional
    public void deleteAllFromFolder(UUID folderId) {
        List<Material> materials = repository.findByFolderId(folderId);

        for (Material material : materials) {
            // Delete all listening tasks associated with this material
            List<ListeningTask> tasks = listeningTaskRepository.findByMaterialId(material.getId());
            listeningTaskRepository.deleteAll(tasks);
        }

        repository.deleteAll(materials);
    }

    @Transactional(readOnly = true)
    public List<ListeningTask> getTasksForMaterial(UUID materialId) {
        if (!repository.existsById(materialId)) {
            throw new EntityNotFoundException("Material not found with id: " + materialId);
        }

        return listeningTaskRepository.findByMaterialId(materialId);
    }

    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return repository.findAllTags();
    }
}

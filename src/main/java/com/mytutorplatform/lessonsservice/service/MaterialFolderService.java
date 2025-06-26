package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.MaterialFolder;
import com.mytutorplatform.lessonsservice.model.response.CreateMaterialFolderRequest;
import com.mytutorplatform.lessonsservice.model.response.MaterialFolderTreeDto;
import com.mytutorplatform.lessonsservice.repository.MaterialFolderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterialFolderService {

    private final MaterialFolderRepository repo;

    public MaterialFolder create(CreateMaterialFolderRequest req) {
        MaterialFolder parent = null;
        if (req.getParentId() != null) {
            parent = repo.findById(req.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
        }
        MaterialFolder folder = MaterialFolder.builder()
                .name(req.getName())
                .parent(parent)
                .build();
        return repo.save(folder);
    }

    @Transactional(readOnly = true)
    public List<MaterialFolder> findAll() {
        return repo.findAll();
    }

    /** builds a nested tree for the UI */
    @Transactional(readOnly = true)
    public List<MaterialFolderTreeDto> getTree() {
        List<MaterialFolder> all = repo.findAll();
        Map<UUID, MaterialFolderTreeDto> map = new HashMap<>();
        all.forEach(f -> map.put(
                f.getId(),
                new MaterialFolderTreeDto(f.getId(), f.getName(), new ArrayList<>())
        ));

        List<MaterialFolderTreeDto> roots = new ArrayList<>();
        all.forEach(f -> {
            MaterialFolderTreeDto dto = map.get(f.getId());
            if (f.getParent() == null) {
                roots.add(dto);
            } else {
                map.get(f.getParent().getId()).getChildren().add(dto);
            }
        });
        return roots;
    }
}
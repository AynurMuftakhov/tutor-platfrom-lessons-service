package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.MaterialFolder;
import com.mytutorplatform.lessonsservice.model.response.CreateMaterialFolderRequest;
import com.mytutorplatform.lessonsservice.model.response.MaterialFolderDTO;
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
    private final MaterialService materialService;

    public MaterialFolderDTO create(CreateMaterialFolderRequest req) {
        MaterialFolder parent = null;
        if (req.getParentId() != null) {
            parent = repo.findById(req.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
        }
        MaterialFolder folder = MaterialFolder.builder()
                .name(req.getName())
                .parent(parent)
                .build();
        return MaterialFolderDTO.from(repo.save(folder));
    }

    public MaterialFolderDTO update(UUID id, CreateMaterialFolderRequest req) {
        MaterialFolder folder = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Folder not found"));

        if (req.getName() != null) {
            folder.setName(req.getName());
        }

        if (req.getParentId() != null) {
            MaterialFolder parent = repo.findById(req.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
            folder.setParent(parent);
        }

        return MaterialFolderDTO.from(repo.save(folder));
    }

    @Transactional
    public void delete(UUID id) {
        materialService.deleteAllFromFolder(id);
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MaterialFolder> findAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<MaterialFolder> findByParentId(UUID parentId) {
        return repo.findByParentId(parentId);
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

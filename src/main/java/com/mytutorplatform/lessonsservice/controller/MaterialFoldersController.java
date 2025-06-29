package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.MaterialFolder;
import com.mytutorplatform.lessonsservice.model.response.CreateMaterialFolderRequest;
import com.mytutorplatform.lessonsservice.model.response.MaterialFolderDTO;
import com.mytutorplatform.lessonsservice.model.response.MaterialFolderTreeDto;
import com.mytutorplatform.lessonsservice.service.MaterialFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/material-folders")
@RequiredArgsConstructor
public class MaterialFoldersController {

    private final MaterialFolderService service;

    @PostMapping
    public ResponseEntity<MaterialFolderDTO> create(@RequestBody CreateMaterialFolderRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public List<MaterialFolder> list(@RequestParam(required = false) UUID parentId) {
        if (parentId != null) {
            return service.findByParentId(parentId);
        }
        return service.findAll();
    }

    @GetMapping("/tree")
    public List<MaterialFolderTreeDto> tree() {
        return service.getTree();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MaterialFolderDTO> update(@PathVariable UUID id, @RequestBody CreateMaterialFolderRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.MaterialFolder;
import com.mytutorplatform.lessonsservice.model.response.CreateMaterialFolderRequest;
import com.mytutorplatform.lessonsservice.model.response.MaterialFolderTreeDto;
import com.mytutorplatform.lessonsservice.service.MaterialFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/material-folders")
@RequiredArgsConstructor
public class MaterialFoldersController {

    private final MaterialFolderService service;

    @PostMapping
    public ResponseEntity<MaterialFolder> create(@RequestBody CreateMaterialFolderRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public List<MaterialFolder> list() {
        return service.findAll();
    }

    @GetMapping("/tree")
    public List<MaterialFolderTreeDto> tree() {
        return service.getTree();
    }
}
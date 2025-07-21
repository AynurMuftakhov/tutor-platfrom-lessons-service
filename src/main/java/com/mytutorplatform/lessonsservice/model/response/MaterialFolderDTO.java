package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.MaterialFolder;

import java.util.UUID;

public record MaterialFolderDTO(UUID id, String name, UUID parentId) {
    public static MaterialFolderDTO from(MaterialFolder folder) {
        return new MaterialFolderDTO(
                folder.getId(),
                folder.getName(),
                folder.getParent() != null ? folder.getParent().getId() : null
        );
    }
}
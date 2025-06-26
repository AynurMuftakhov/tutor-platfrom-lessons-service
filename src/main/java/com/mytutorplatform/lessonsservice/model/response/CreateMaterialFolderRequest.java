package com.mytutorplatform.lessonsservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialFolderRequest {
    private String name;
    private UUID parentId;      // optional
}

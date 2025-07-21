package com.mytutorplatform.lessonsservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialFolderTreeDto {
    private UUID id;
    private String name;
    private List<MaterialFolderTreeDto> children = new ArrayList<>();
}

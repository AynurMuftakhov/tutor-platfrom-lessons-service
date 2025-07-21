package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.Material;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialDTO {
        private UUID id;
        private String title;
        private Material.AssetType type;
        private String sourceUrl;
        private String thumbnailUrl;
        private Integer durationSec;
        private Set<String> tags;
        private UUID folderId;
}

package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    @Column(nullable = false)
    private String sourceUrl;

    private String thumbnailUrl;

    private Integer durationSec;

    @ElementCollection
    @CollectionTable(name = "material_tags", joinColumns = @JoinColumn(name = "material_id"))
    @Column(name = "tag")
    private Set<String> tags;

    @Column(name = "folder_id")
    private UUID folderId;

    public enum AssetType {
        VIDEO,
        AUDIO,
        DOCUMENT,
        GRAMMAR,
        LISTENING
    }
}

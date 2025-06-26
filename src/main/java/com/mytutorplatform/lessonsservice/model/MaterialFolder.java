package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "material_folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialFolder {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    /** self-reference for nesting */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MaterialFolder parent;

    /** not serialized in controllers to avoid infinite recursion */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MaterialFolder> children = new HashSet<>();
}
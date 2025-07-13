package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "grammar_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarItem {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "material_id")
    private UUID materialId;

    private Integer sortOrder;
    private Type type;

    @Column(columnDefinition = "TEXT")
    private String text;

    //structure varies by type
    private String metadata;

    @Column(columnDefinition = "TEXT")
    private String answer;

    public enum Type {
        GAP_FILL
    }
}

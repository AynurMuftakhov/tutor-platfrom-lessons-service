package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "listening_tasks")
@Data
public class ListeningTask {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    @Column(nullable = false)
    private Integer startSec;

    @Column(nullable = false)
    private Integer endSec;

    private Integer wordLimit;

    private Integer timeLimitSec;

    @Column(name = "material_id")
    private UUID materialId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

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

    @Column(nullable = false)
    private UUID lessonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(nullable = false)
    private Integer startSec;

    @Column(nullable = false)
    private Integer endSec;

    private Integer wordLimit;

    private Integer timeLimitSec;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum AssetType {
        VIDEO, AUDIO
    }
}
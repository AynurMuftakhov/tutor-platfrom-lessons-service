package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "transcript_id")
    private UUID transcriptId;

    @Lob
    @Column(name = "transcript_text", columnDefinition = "text")
    private String transcriptText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "listening_task_target_words", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "word")
    private List<String> targetWords = new ArrayList<>();

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "language_code")
    private String language;

    @Column(name = "difficulty_level")
    private String difficulty;

    @Lob
    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ListeningTaskStatus status = ListeningTaskStatus.DRAFT;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

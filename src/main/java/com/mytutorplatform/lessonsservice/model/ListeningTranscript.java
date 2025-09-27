package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "listening_transcripts")
@Getter
@Setter
public class ListeningTranscript {

    @Id
    @Column(name = "transcript_id", nullable = false, updatable = false)
    private UUID transcriptId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Lob
    @Column(name = "text", nullable = false)
    private String text;

    @Lob
    @Column(name = "words_json", nullable = false, columnDefinition = "text")
    private String wordsJson;

    @Lob
    @Column(name = "coverage_json", columnDefinition = "text")
    private String coverageJson;

    @Lob
    @Column(name = "metadata_json", columnDefinition = "text")
    private String metadataJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

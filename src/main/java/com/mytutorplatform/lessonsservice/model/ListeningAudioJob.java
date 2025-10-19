package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "listening_audio_jobs", indexes = {
        @Index(name = "idx_laj_teacher_created", columnList = "teacher_id, created_at DESC")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_laj_idempotency", columnNames = {"idempotency_key"})
})
@Getter
@Setter
public class ListeningAudioJob {

    @Id
    @Column(name = "job_id", nullable = false, updatable = false)
    private UUID jobId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "transcript_id", nullable = false)
    private UUID transcriptId;

    @Lob
    @Column(name = "transcript_text", nullable = false)
    private String transcriptText;

    @Column(name = "voice_id", nullable = false)
    private String voiceId;

    @Column(name = "tts_model", nullable = false)
    private String ttsModel;

    @Column(name = "language_code")
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "audio_material_id")
    private UUID audioMaterialId;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Lob
    @Column(name = "error_json", columnDefinition = "text")
    private String errorJson;

    @Lob
    @Column(name = "request_json", nullable = false, columnDefinition = "text")
    private String requestJson;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum JobStatus { PENDING, RUNNING, SUCCEEDED, FAILED, CANCELLED, EXPIRED }
}

package com.mytutorplatform.lessonsservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lessons", indexes = {
        @Index(name = "idx_lessons_student_teacher_date", columnList = "studentId,tutorId,dateTime DESC"),
        @Index(name = "idx_lessons_tutor_status_datetime", columnList = "tutorId,status,dateTime"),
        @Index(name = "idx_lessons_student_status_datetime", columnList = "studentId,status,dateTime"),
        @Index(name = "idx_lessons_tutor_student_status_datetime", columnList = "tutorId,studentId,status,dateTime"),
        @Index(name = "idx_lessons_tutor_datetime", columnList = "tutorId,dateTime")
})
@Data
public class Lesson {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    @Column(nullable = false)
    private OffsetDateTime dateTime;

    @Column
    private OffsetDateTime endDate;

    @Column(nullable = false)
    private int duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonStatus status = LessonStatus.SCHEDULED;

    @Column(nullable = false)
    private UUID tutorId;

    @Column(nullable = false)
    private UUID studentId;

    private String location;

    private String lessonPlan;

    private String learningObjectives;

    private String notes;

    private String studentPerformance;

    private String homework;

    @Enumerated(EnumType.STRING)
    private LessonSatisfaction lessonSatisfaction;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonAttachment> attachments;

    // Ensure lesson materials are automatically removed when a lesson is deleted
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<LessonMaterial> materials;

    // Ensure lesson content links are automatically removed when a lesson is deleted
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<LessonContentLink> contentLinks;

    private List<UUID> taskIds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    @JsonIgnore
    private RecurringLessonSeries series;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

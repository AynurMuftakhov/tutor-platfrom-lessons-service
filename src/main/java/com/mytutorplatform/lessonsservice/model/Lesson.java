package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    @Column(nullable = false)
    private OffsetDateTime dateTime;

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

    @Enumerated(EnumType.STRING)
    private LessonSatisfaction lessonSatisfaction;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonAttachment> attachments;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {
    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private LocalDateTime dateTime;
    private int duration;

    @Enumerated(EnumType.STRING)
    private LessonStatus status;

    private BigDecimal price;

    private UUID tutorId;
    private UUID studentId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

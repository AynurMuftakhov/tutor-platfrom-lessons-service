package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "recurring_lesson_series")
@Data
public class RecurringLessonSeries {

    @Id
    @GeneratedValue
    private UUID seriesId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceFrequency frequency;

    @Column(nullable = false)
    private Integer interval;

    private OffsetDateTime until;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum RecurrenceFrequency {
        WEEKLY
        // Can be extended in the future with DAILY, MONTHLY, etc.
    }
}
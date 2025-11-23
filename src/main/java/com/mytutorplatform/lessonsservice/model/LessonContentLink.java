package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "lesson_content_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonContentLink {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_content_id", nullable = false)
    private LessonContent lessonContent;

    @Column(nullable = false)
    private int sortOrder;
}

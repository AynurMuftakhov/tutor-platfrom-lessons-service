package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "lesson_attachments")
@Data
public class LessonAttachment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    private String fileName;
    private String fileUrl;
}

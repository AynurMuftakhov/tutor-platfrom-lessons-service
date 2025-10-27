package com.mytutorplatform.lessonsservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Notes per Lesson: one optional note per lesson.
 * Ownership: either lesson's tutor or student may read/write.
 */
@Entity
@Table(name = "lesson_notes", indexes = {
        @Index(name = "idx_lesson_notes_updated_at", columnList = "updated_at")
})
@Getter
@Setter
@NoArgsConstructor
@ToString
public class LessonNote {

    @Id
    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "lesson_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_lesson_notes_lesson"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lesson lesson;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "format", nullable = false)
    private String format = "md"; // 'md' or 'plain'

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;
}

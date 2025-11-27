package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.LessonNote;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for lesson note responses.
 */
public record LessonNoteResponse(
        UUID lessonId,
        String content,
        String format,
        OffsetDateTime updatedAt,
        UUID updatedBy
) {
    public static LessonNoteResponse from(LessonNote note) {
        return new LessonNoteResponse(
                note.getLessonId(),
                note.getContent(),
                note.getFormat(),
                note.getUpdatedAt(),
                note.getUpdatedBy()
        );
    }
}

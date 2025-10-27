package com.mytutorplatform.lessonsservice.repository.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface NotesLessonRow {
    UUID getLessonId();
    OffsetDateTime getScheduledAt();
    OffsetDateTime getUpdatedAt();
}

package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonNote;
import com.mytutorplatform.lessonsservice.repository.LessonNoteRepository;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.projection.NotesLessonRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonNotesService {

    private static final Set<String> ALLOWED_FORMATS = Set.of("md", "plain");

    private final LessonRepository lessonRepository;
    private final LessonNoteRepository noteRepository;

    public Optional<LessonNote> get(UUID lessonId) {
        requireLesson(lessonId);
        return noteRepository.findById(lessonId);
    }

    @Transactional
    public UpsertResult put(UUID lessonId, UUID callerId, String content, String formatOpt) {
      Lesson lesson = requireLesson(lessonId);

        String trimmed = content == null ? null : content.trim();
        if (trimmed == null) {
            throw new IllegalArgumentException("content is required");
        }
        // delete-by-empty
        if (trimmed.isEmpty()) {
            boolean existed = noteRepository.existsById(lessonId);
            if (existed) {
                noteRepository.deleteById(lessonId);
            }
            log.info("PUT lesson note: lessonId={}, userId={}, size=0, result=204", lessonId, callerId);
            return UpsertResult.deleted();
        }

        String format = (formatOpt == null || formatOpt.isBlank()) ? "md" : formatOpt.toLowerCase(Locale.ROOT);
        if (!ALLOWED_FORMATS.contains(format)) {
            throw new UnprocessableException("Invalid format: " + format);
        }

        OffsetDateTime now = OffsetDateTime.now();
        LessonNote note = noteRepository.findById(lessonId).orElse(null);
        if (note == null) {
            note = new LessonNote();
            note.setLesson(lesson);
            note.setContent(trimmed);
            note.setFormat(format);
            note.setUpdatedAt(now);
            note.setUpdatedBy(callerId);
            LessonNote saved = noteRepository.save(note);
            log.info("PUT lesson note: lessonId={}, userId={}, size={}, result=201", lessonId, callerId, trimmed.length());
            return UpsertResult.created(saved);
        } else {
            note.setContent(trimmed);
            if (formatOpt != null) {
                note.setFormat(format); // allow updating format
            }
            note.setUpdatedAt(now);
            note.setUpdatedBy(callerId);
            LessonNote saved = noteRepository.save(note);
            log.info("PUT lesson note: lessonId={}, userId={}, size={}, result=200", lessonId, callerId, trimmed.length());
            return UpsertResult.updated(saved);
        }
    }

    public PagedResult listPreviousWithNotes(UUID studentId, UUID teacherId, int limit, String cursor) {
        if (limit <= 0 || limit > 100) {
            throw new UnprocessableException("Invalid limit");
        }
        // parse cursor
        OffsetDateTime updatedBefore = null;
        UUID lessonIdBefore = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                String[] parts = decoded.split("\\|", 2);
                updatedBefore = OffsetDateTime.parse(parts[0]);
                lessonIdBefore = UUID.fromString(parts[1]);
            } catch (Exception e) {
                log.warn("Invalid cursor: {}", cursor);
                throw new UnprocessableException("Invalid cursor");
            }
        }
        int pageSize = Math.min(limit, 100);
        int fetchSize = pageSize + 1; // fetch one extra to compute next cursor
        Pageable pageable = PageRequest.of(0, fetchSize);

        List<NotesLessonRow> rows;
        if (updatedBefore != null) {
            rows = noteRepository.findNotesLessonsAfter(studentId, teacherId, updatedBefore, lessonIdBefore, pageable);
        } else {
            rows = noteRepository.findNotesLessons(studentId, teacherId, pageable);
        }
        List<PagedItem> items = new ArrayList<>();
        String nextCursor = null;

        int resultCount = Math.min(pageSize, rows.size());
        for (int i = 0; i < resultCount; i++) {
            var r = rows.get(i);
            items.add(new PagedItem(r.getLessonId(), r.getScheduledAt(), r.getUpdatedAt()));
        }
        if (rows.size() > pageSize) {
            var last = rows.get(pageSize - 1);
            nextCursor = Base64.getUrlEncoder().encodeToString((last.getUpdatedAt().toString() + "|" + last.getLessonId()).getBytes(StandardCharsets.UTF_8));
        }
        return new PagedResult(items, nextCursor);
    }

    private Lesson requireLesson(UUID lessonId) {
        return lessonRepository.findById(lessonId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Lesson not found"));
    }


    public static class UpsertResult {
        public enum Kind { CREATED, UPDATED, DELETED }
        public final Kind kind;
        public final LessonNote note;
        private UpsertResult(Kind kind, LessonNote note) { this.kind = kind; this.note = note; }
        public static UpsertResult created(LessonNote n){ return new UpsertResult(Kind.CREATED, n);}    
        public static UpsertResult updated(LessonNote n){ return new UpsertResult(Kind.UPDATED, n);}    
        public static UpsertResult deleted(){ return new UpsertResult(Kind.DELETED, null);}    
    }

    public record PagedItem(UUID lessonId, OffsetDateTime scheduledAt, OffsetDateTime updatedAt) {}
    public record PagedResult(List<PagedItem> items, String nextCursor) {}

    public static class ForbiddenException extends RuntimeException { public ForbiddenException(String m){ super(m);} }
    public static class UnprocessableException extends RuntimeException { public UnprocessableException(String m){ super(m);} }
}

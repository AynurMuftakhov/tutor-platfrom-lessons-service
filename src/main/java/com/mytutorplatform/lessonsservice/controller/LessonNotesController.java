package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.LessonNote;
import com.mytutorplatform.lessonsservice.service.LessonNotesService;
import com.mytutorplatform.lessonsservice.service.LessonNotesService.PagedResult;
import com.mytutorplatform.lessonsservice.service.LessonNotesService.UpsertResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LessonNotesController {

    private final LessonNotesService notesService;

    @GetMapping("/lessons/{lessonId}/notes")
    public ResponseEntity<?> getNote(@PathVariable UUID lessonId, @RequestHeader("X-User-ID") UUID userId) {
        try {
            Optional<LessonNote> note = notesService.get(lessonId);
            if (note.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            LessonNote n = note.get();
            return ResponseEntity.ok(Map.of(
                    "lessonId", n.getLessonId(),
                    "content", n.getContent(),
                    "format", n.getFormat(),
                    "updatedAt", n.getUpdatedAt(),
                    "updatedBy", n.getUpdatedBy()
            ));
        } catch (LessonNotesService.ForbiddenException e) {
            log.warn("Forbidden get note for lesson {} by {}", lessonId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error getting note", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /api/lessons/{lessonId}/notes
    @PutMapping("/lessons/{lessonId}/notes")
    public ResponseEntity<?> putNote(@PathVariable UUID lessonId,
                                     @RequestHeader("X-User-ID") UUID userId,
                                     @RequestBody Map<String, Object> body,
                                     HttpServletRequest request) {
        String requestId = Optional.ofNullable(request.getHeader("X-Request-ID")).orElse("-");
        try {
            String content = body.get("content") == null ? null : String.valueOf(body.get("content"));
            String format = body.get("format") == null ? null : String.valueOf(body.get("format"));
            UpsertResult result = notesService.put(lessonId, userId, content, format);
            if (result.kind == UpsertResult.Kind.DELETED) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            LessonNote n = result.note;
            Map<String, Object> payload = Map.of(
                    "lessonId", n.getLessonId(),
                    "content", n.getContent(),
                    "format", n.getFormat(),
                    "updatedAt", n.getUpdatedAt(),
                    "updatedBy", n.getUpdatedBy()
            );
            if (result.kind == UpsertResult.Kind.CREATED) {
                return ResponseEntity.status(HttpStatus.CREATED).body(payload);
            } else {
                return ResponseEntity.ok(payload);
            }
        } catch (LessonNotesService.UnprocessableException e) {
            log.warn("422 on put note lessonId={}, userId={}, reason={}", lessonId, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", e.getMessage()));
        } catch (LessonNotesService.ForbiddenException e) {
            log.warn("Forbidden put note for lesson {} by {}", lessonId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error putting note [{}]", requestId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/notes/lessons")
    public ResponseEntity<?> listLessonsWithNotes(@RequestParam UUID studentId,
                                                  @RequestParam UUID teacherId,
                                                  @RequestParam(defaultValue = "20") int limit,
                                                  @RequestParam(required = false) String cursor,
                                                  @RequestHeader("X-User-ID") UUID userId) {
        try {
            PagedResult result = notesService.listPreviousWithNotes(studentId, teacherId, limit, cursor);
            return ResponseEntity.ok(result);
        } catch (LessonNotesService.UnprocessableException e) {
            log.warn("422 on list notes lessons: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", e.getMessage()));
        } catch (LessonNotesService.ForbiddenException e) {
            log.warn("Forbidden list notes lessons for student {} teacher {} by {}", studentId, teacherId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error listing lessons with notes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.LessonNote;
import com.mytutorplatform.lessonsservice.repository.projection.NotesLessonRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LessonNoteRepository extends JpaRepository<LessonNote, UUID> {

    @Query("select l.id as lessonId, l.dateTime as scheduledAt, ln.updatedAt as updatedAt " +
            "from LessonNote ln join ln.lesson l " +
            "where l.studentId = :studentId and l.tutorId = :teacherId " +
            "order by ln.updatedAt desc, l.id desc")
    List<NotesLessonRow> findNotesLessons(
            @Param("studentId") UUID studentId,
            @Param("teacherId") UUID teacherId,
            Pageable pageable);

    @Query("select l.id as lessonId, l.dateTime as scheduledAt, ln.updatedAt as updatedAt " +
            "from LessonNote ln join ln.lesson l " +
            "where l.studentId = :studentId and l.tutorId = :teacherId and (ln.updatedAt < :updatedAt or (ln.updatedAt = :updatedAt and l.id < :lessonId)) " +
            "order by ln.updatedAt desc, l.id desc")
    List<NotesLessonRow> findNotesLessonsAfter(
            @Param("studentId") UUID studentId,
            @Param("teacherId") UUID teacherId,
            @Param("updatedAt") OffsetDateTime updatedAt,
            @Param("lessonId") UUID lessonId,
            Pageable pageable);
}

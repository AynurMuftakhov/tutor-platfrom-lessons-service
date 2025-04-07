package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    @Query("SELECT l FROM Lesson l WHERE l.tutorId = :tutorId AND l.dateTime BETWEEN :start AND :end")
    List<Lesson> findLessonsByTutorAndDateRange(UUID tutorId, OffsetDateTime start, OffsetDateTime end);

    @Query("""
    SELECT l FROM Lesson l
    WHERE (:tutorId IS NULL OR l.tutorId = :tutorId)
    AND (:#{#status == null || #status.isEmpty()} = true OR l.status IN :status)
    order by l.dateTime
""")
    Page<Lesson> findLessonsByTutorIdAndStatus(
            @Param("tutorId") UUID tutorId,
            @Param("status") List<LessonStatus> status,
            Pageable pageable
    );

    @Query("""
    SELECT l FROM Lesson l
    WHERE (:studentId IS NULL OR l.studentId = :studentId)
    AND (:#{#status == null || #status.isEmpty()} = true OR l.status IN :status)
    order by l.dateTime
""")
    Page<Lesson> findLessonsByStudentAndStatus(UUID studentId, @Param("status") List<LessonStatus> status, Pageable pageable);
}

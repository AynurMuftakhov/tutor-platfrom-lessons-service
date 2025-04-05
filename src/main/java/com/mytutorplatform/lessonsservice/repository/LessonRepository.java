package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
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
    AND (:status IS NULL OR l.status = :status)
""")
    List<Lesson> findLessonsByTutorIdAndStatus(
            @Param("tutorId") UUID tutorId,
            @Param("status") LessonStatus status
    );

    @Query("SELECT l FROM Lesson l WHERE l.studentId = :studentId AND l.dateTime BETWEEN :start AND :end")
    List<Lesson> findLessonsByStudentAndDateRange(UUID studentId, OffsetDateTime start, OffsetDateTime end);
}

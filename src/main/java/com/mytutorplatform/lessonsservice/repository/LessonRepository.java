package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    @Query("SELECT l FROM Lesson l WHERE l.tutorId = :tutorId AND l.dateTime BETWEEN :start AND :end")
    List<Lesson> findLessonsByTutorAndDateRange(UUID tutorId, OffsetDateTime start, OffsetDateTime end);

    List<Lesson> findByStudentId(UUID studentId);

    @Query("SELECT l FROM Lesson l WHERE l.studentId = :studentId AND l.dateTime BETWEEN :start AND :end")
    List<Lesson> findLessonsByStudentAndDateRange(UUID studentId, OffsetDateTime start, OffsetDateTime end);
}

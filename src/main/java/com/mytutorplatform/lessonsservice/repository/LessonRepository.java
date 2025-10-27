package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.RecurringLessonSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID>, JpaSpecificationExecutor<Lesson> {
    @Query("SELECT l FROM Lesson l WHERE l.tutorId = :tutorId AND l.dateTime BETWEEN :startDateTime AND :endDateTime")
    List<Lesson> findLessonsByTutorAndDateRange(
        @Param("tutorId") UUID tutorId, 
        @Param("startDateTime") OffsetDateTime startDateTime, 
        @Param("endDateTime") OffsetDateTime endDateTime);

    @Query("SELECT COUNT(DISTINCT l.studentId) FROM Lesson l WHERE l.tutorId = :tutorId AND l.status = :lessonStatus")
    long countDistinctStudentIdsByTutorIdAndStatus(@Param("tutorId") UUID tutorId, @Param("lessonStatus") LessonStatus lessonStatus);

    @Query("SELECT COUNT(DISTINCT l.id) FROM Lesson l WHERE l.tutorId = :tutorId AND l.status = :status AND l.dateTime BETWEEN :startDateTime AND :endDateTime")
    long countByTutorIdAndStatusAndDateTimeBetween(
        @Param("tutorId") UUID tutorId, 
        @Param("status") LessonStatus status, 
        @Param("startDateTime") OffsetDateTime startDateTime, 
        @Param("endDateTime") OffsetDateTime endDateTime);

    List<Lesson> findAllBySeries(RecurringLessonSeries series);
}
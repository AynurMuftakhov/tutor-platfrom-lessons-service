package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.model.RecurringLessonSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            SELECT l FROM Lesson l
            WHERE (l.studentId = :userId OR l.tutorId = :userId)
              AND l.status IN :statuses
              AND l.dateTime >= :fromInclusive
            ORDER BY l.dateTime ASC
            """)
    List<Lesson> findDashboardNextCandidates(
            @Param("userId") UUID userId,
            @Param("statuses") List<LessonStatus> statuses,
            @Param("fromInclusive") OffsetDateTime fromInclusive,
            Pageable pageable
    );

    @Query("""
            SELECT l FROM Lesson l
            WHERE (l.studentId = :userId OR l.tutorId = :userId)
              AND l.status IN :statuses
              AND l.dateTime >= :fromInclusive
              AND l.dateTime <= :toInclusive
            ORDER BY l.dateTime ASC
            """)
    List<Lesson> findDashboardUpcoming(
            @Param("userId") UUID userId,
            @Param("statuses") List<LessonStatus> statuses,
            @Param("fromInclusive") OffsetDateTime fromInclusive,
            @Param("toInclusive") OffsetDateTime toInclusive,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(l.id) FROM Lesson l
            WHERE l.tutorId = :tutorId
              AND l.status = :completedStatus
              AND l.dateTime >= :fromInclusive
              AND l.dateTime <= :toInclusive
              AND (l.notes IS NULL OR TRIM(l.notes) = '')
            """)
    long countDashboardMissingNotesForTutor(
            @Param("tutorId") UUID tutorId,
            @Param("completedStatus") LessonStatus completedStatus,
            @Param("fromInclusive") OffsetDateTime fromInclusive,
            @Param("toInclusive") OffsetDateTime toInclusive
    );

    @Query("""
            SELECT COUNT(DISTINCT l.studentId) FROM Lesson l
            WHERE l.tutorId = :tutorId
              AND l.status IN :historicalStatuses
              AND l.dateTime >= :recentFromInclusive
              AND l.dateTime <= :recentToInclusive
              AND NOT EXISTS (
                  SELECT 1 FROM Lesson u
                  WHERE u.tutorId = :tutorId
                    AND u.studentId = l.studentId
                    AND u.status IN :upcomingStatuses
                    AND u.dateTime >= :upcomingFromInclusive
              )
            """)
    long countDashboardStudentsWithoutNextLesson(
            @Param("tutorId") UUID tutorId,
            @Param("historicalStatuses") List<LessonStatus> historicalStatuses,
            @Param("recentFromInclusive") OffsetDateTime recentFromInclusive,
            @Param("recentToInclusive") OffsetDateTime recentToInclusive,
            @Param("upcomingStatuses") List<LessonStatus> upcomingStatuses,
            @Param("upcomingFromInclusive") OffsetDateTime upcomingFromInclusive
    );

    @Query("""
            SELECT COUNT(l.id) FROM Lesson l
            WHERE l.studentId = :studentId
              AND l.status = :status
              AND l.dateTime >= :fromInclusive
              AND l.dateTime <= :toInclusive
            """)
    long countDashboardCompletedThisMonthForStudent(
            @Param("studentId") UUID studentId,
            @Param("status") LessonStatus status,
            @Param("fromInclusive") OffsetDateTime fromInclusive,
            @Param("toInclusive") OffsetDateTime toInclusive
    );

    @Query("""
            SELECT l FROM Lesson l
            WHERE l.tutorId = :tutorId
              AND l.dateTime >= :fromInclusive
              AND l.dateTime < :toExclusive
            ORDER BY l.dateTime ASC
            """)
    List<Lesson> findDashboardTutorTodayAgenda(
            @Param("tutorId") UUID tutorId,
            @Param("fromInclusive") OffsetDateTime fromInclusive,
            @Param("toExclusive") OffsetDateTime toExclusive,
            Pageable pageable
    );

    long countByStudentIdOrTutorId(UUID studentId, UUID tutorId);

    List<Lesson> findAllBySeries(RecurringLessonSeries series);
}

package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.LessonContentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LessonContentLinkRepository extends JpaRepository<LessonContentLink, UUID> {

    List<LessonContentLink> findByLessonIdOrderBySortOrder(UUID lessonId);

    @Query("SELECT MAX(lcl.sortOrder) FROM LessonContentLink lcl WHERE lcl.lesson.id = :lessonId")
    Integer findMaxSortOrderByLessonId(@Param("lessonId") UUID lessonId);

    boolean existsByLessonIdAndLessonContentId(UUID lessonId, UUID lessonContentId);
}

package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.LessonMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, UUID> {
    
    List<LessonMaterial> findByLessonIdOrderBySortOrder(UUID lessonId);
    
    @Query("SELECT MAX(lm.sortOrder) FROM LessonMaterial lm WHERE lm.lesson.id = :lessonId")
    Integer findMaxSortOrderByLessonId(@Param("lessonId") UUID lessonId);
}
package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.LessonContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LessonContentRepository extends JpaRepository<LessonContent, UUID> {

    Page<LessonContent> findAllByOwnerId(UUID ownerId, Pageable pageable);

    @Query(value = "select * from lesson_contents lc where lc.owner_id = :ownerId and lc.title ilike concat('%', :q, '%')",
            countQuery = "select count(*) from lesson_contents lc where lc.owner_id = :ownerId and lc.title ilike concat('%', :q, '%')",
            nativeQuery = true)
    Page<LessonContent> searchByOwnerIdAndTitleIlike(@Param("ownerId") UUID ownerId, @Param("q") String q, Pageable pageable);
}

package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID>, JpaSpecificationExecutor<Material> {
    List<Material> findByFolderId(UUID folderId);

    @Query("SELECT DISTINCT t FROM Material m JOIN m.tags t")
    List<String> findAllTags();
}

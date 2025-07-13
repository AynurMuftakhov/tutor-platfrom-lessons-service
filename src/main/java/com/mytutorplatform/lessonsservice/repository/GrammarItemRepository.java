package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GrammarItemRepository extends JpaRepository<GrammarItem, UUID> {
    List<GrammarItem> findByMaterialId(UUID materialId);
}

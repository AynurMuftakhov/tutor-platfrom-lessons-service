package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.MaterialFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaterialFolderRepository extends JpaRepository<MaterialFolder, UUID> {
    List<MaterialFolder> findByParentIsNull();
}
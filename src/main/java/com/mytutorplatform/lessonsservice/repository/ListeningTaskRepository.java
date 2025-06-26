package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.ListeningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListeningTaskRepository extends JpaRepository<ListeningTask, UUID>, JpaSpecificationExecutor<ListeningTask> {
    List<ListeningTask> findByFolderId(UUID folderId);
}
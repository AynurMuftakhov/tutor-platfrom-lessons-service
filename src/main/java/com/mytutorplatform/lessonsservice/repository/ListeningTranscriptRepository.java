package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.ListeningTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ListeningTranscriptRepository extends JpaRepository<ListeningTranscript, UUID> {
}

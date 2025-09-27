package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.ListeningAudioJob;
import com.mytutorplatform.lessonsservice.model.ListeningAudioJob.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListeningAudioJobRepository extends JpaRepository<ListeningAudioJob, UUID> {
    Optional<ListeningAudioJob> findByIdempotencyKey(String idempotencyKey);
    List<ListeningAudioJob> findByStatusIn(Collection<JobStatus> statuses);
    List<ListeningAudioJob> findByStatusAndCreatedAtBefore(JobStatus status, OffsetDateTime before);
}

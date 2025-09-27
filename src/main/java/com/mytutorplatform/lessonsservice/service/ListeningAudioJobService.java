package com.mytutorplatform.lessonsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytutorplatform.lessonsservice.model.ListeningAudioJob;
import com.mytutorplatform.lessonsservice.model.ListeningAudioJob.JobStatus;
import com.mytutorplatform.lessonsservice.model.ListeningTranscript;
import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.model.request.AudioGenerateRequest;
import com.mytutorplatform.lessonsservice.repository.ListeningAudioJobRepository;
import com.mytutorplatform.lessonsservice.repository.ListeningTranscriptRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningAudioJobService {

    private final ListeningAudioJobRepository jobRepository;
    private final ListeningTranscriptRepository transcriptRepository;
    private final ElevenLabsClient elevenLabsClient;
    private final LocalAudioStorageService audioStorageService;
    private final MaterialRepository materialRepository;
    private final ObjectMapper objectMapper;

    private ExecutorService executor;

    @Value("${listening.audio.poolSize:4}")
    private int poolSize;
    @Value("${listening.audio.jobTtlHours:24}")
    private int jobTtlHours;
    @Value("${elevenlabs.defaultVoiceId:JBFqnCBsd6RMkjVDRZzb}")
    private String defaultVoiceId;
    @Value("${elevenlabs.model:eleven_multilingual_v2}")
    private String defaultTtsModel;

    @Value("${listening.transcript.targetWpm:140}")
    private int targetWpm;

    @PostConstruct
    public void init() {
        this.executor = Executors.newFixedThreadPool(Math.max(1, poolSize));
        expireOldAndResume();
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) executor.shutdownNow();
    }

    public ListeningAudioJob startJob(AudioGenerateRequest request, String idempotencyKey, UUID teacherId, String requestId) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<ListeningAudioJob> existing = jobRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) return existing.get();
        }

        // Resolve transcript
        String text;
        UUID transcriptId = request.transcriptId();
        if (request.transcriptOverride() != null && !request.transcriptOverride().isBlank()) {
            text = request.transcriptOverride();
            if (transcriptId == null) {
                throw new IllegalArgumentException("transcriptId is required when using override (for ownership tracking)");
            }
            // Also verify ownership if transcript exists
            ListeningTranscript t = transcriptRepository.findById(transcriptId)
                    .orElseThrow(() -> new EntityNotFoundException("Transcript not found: " + transcriptId));
            if (!t.getTeacherId().equals(teacherId)) {
                throw new IllegalArgumentException("Transcript does not belong to teacher");
            }
        } else {
            if (transcriptId == null) throw new IllegalArgumentException("transcriptId is required");
            ListeningTranscript t = transcriptRepository.findById(transcriptId)
                    .orElseThrow(() -> new EntityNotFoundException("Transcript not found: " + transcriptId));
            if (!t.getTeacherId().equals(teacherId)) {
                throw new IllegalArgumentException("Transcript does not belong to teacher");
            }
            text = t.getText();
        }

        String norm = normalizeText(text);

        ListeningAudioJob job = new ListeningAudioJob();
        job.setJobId(UUID.randomUUID());
        job.setTeacherId(teacherId);
        job.setTranscriptId(transcriptId);
        job.setTranscriptText(norm);
        job.setVoiceId(request.voiceId() != null && !request.voiceId().isBlank() ? request.voiceId() : defaultVoiceId);
        job.setTtsModel(request.ttsModel() != null && !request.ttsModel().isBlank() ? request.ttsModel() : defaultTtsModel);
        job.setLanguageCode(request.languageCode());
        job.setStatus(JobStatus.PENDING);
        job.setIdempotencyKey(idempotencyKey);
        try {
            job.setRequestJson(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            job.setRequestJson("{}");
        }
        jobRepository.save(job);

        executor.submit(() -> runJob(job.getJobId(), request, requestId));
        return job;
    }

    public Optional<ListeningAudioJob> getJob(UUID jobId) {
        return jobRepository.findById(jobId);
    }

    private void runJob(UUID jobId, AudioGenerateRequest req, String requestId) {
        ListeningAudioJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;
        try {
            job.setStatus(JobStatus.RUNNING);
            jobRepository.save(job);

            byte[] mp3 = elevenLabsClient.createSpeech(
                    job.getTranscriptText(),
                    job.getVoiceId(),
                    req.ttsModel(),
                    req.languageCode(),
                    req.voiceSettings(),
                    req.outputFormat(),
                    requestId
            );

            LocalAudioStorageService.StoredAudio stored = audioStorageService.store(mp3, "mp3");

            // Create Material entry
            Material material = Material.builder()
                    .title("Listening Audio " + job.getTranscriptId())
                    .type(Material.AssetType.AUDIO)
                    .sourceUrl(stored.url())
                    .durationSec(estimateDuration(job.getTranscriptText(), req.voiceSettings() != null ? req.voiceSettings().speed() : null))
                    .build();
            material = materialRepository.save(material);

            job.setAudioMaterialId(material.getId());
            job.setAudioUrl(material.getSourceUrl());
            job.setDurationSec(material.getDurationSec());
            job.setStatus(JobStatus.SUCCEEDED);
            jobRepository.save(job);
        } catch (Exception e) {
            log.error("Audio job {} failed: {}", jobId, e.getMessage(), e);
            try {
                job.setStatus(JobStatus.FAILED);
                job.setErrorJson('{' + "\"message\":\"" + safe(e.getMessage()) + "\"" + '}');
                jobRepository.save(job);
            } catch (Exception ignored) {}
        }
    }

    private String safe(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }

    private int estimateDuration(String text, Double speed) {
        int words = countWords(text);
        double wpm = (speed != null && speed > 0) ? (targetWpm * speed) : targetWpm;
        return (int) Math.max(1, Math.round((words / Math.max(1.0, wpm)) * 60.0));
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        String[] parts = text.trim().split("\\s+");
        int c = 0;
        for (String p : parts) if (!p.isBlank()) c++;
        return c;
    }

    private static final Pattern TAGS = Pattern.compile("<[^>]+>");
    private String normalizeText(String s) {
        if (s == null) return "";
        String noTags = TAGS.matcher(s).replaceAll(" ");
        String collapsed = noTags.replaceAll("[\r\n\t]+", " ").replaceAll(" +", " ").trim();
        return collapsed;
    }

    private void expireOldAndResume() {
        try {
            // Expire PENDING and RUNNING older than TTL
            var cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusHours(jobTtlHours);
            List<ListeningAudioJob> oldPending = jobRepository.findByStatusAndCreatedAtBefore(JobStatus.PENDING, cutoff);
            ListeningAudioJob[] states = {};
            for (ListeningAudioJob j : oldPending) j.setStatus(JobStatus.EXPIRED);
            if (!oldPending.isEmpty()) jobRepository.saveAll(oldPending);
            List<ListeningAudioJob> oldRunning = jobRepository.findByStatusAndCreatedAtBefore(JobStatus.RUNNING, cutoff);
            for (ListeningAudioJob j : oldRunning) j.setStatus(JobStatus.EXPIRED);
            if (!oldRunning.isEmpty()) jobRepository.saveAll(oldRunning);

            // Resume RUNNING and PENDING (not expired)
            List<ListeningAudioJob> toResume = jobRepository.findByStatusIn(List.of(JobStatus.RUNNING, JobStatus.PENDING));
            for (ListeningAudioJob j : toResume) {
                try {
                    AudioGenerateRequest req = objectMapper.readValue(j.getRequestJson(), AudioGenerateRequest.class);
                    executor.submit(() -> runJob(j.getJobId(), req, UUID.randomUUID().toString()));
                } catch (Exception e) {
                    log.warn("Failed to resume job {}: {}", j.getJobId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Expire/resume failed: {}", e.getMessage());
        }
    }
}

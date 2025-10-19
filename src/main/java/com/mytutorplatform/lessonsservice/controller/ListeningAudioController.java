package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.ListeningAudioJob;
import com.mytutorplatform.lessonsservice.model.request.AudioGenerateRequest;
import com.mytutorplatform.lessonsservice.model.response.JobStatusResponse;
import com.mytutorplatform.lessonsservice.model.response.StartJobResponse;
import com.mytutorplatform.lessonsservice.service.ElevenLabsClient;
import com.mytutorplatform.lessonsservice.service.ListeningAudioJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/listening")
@RequiredArgsConstructor
public class ListeningAudioController {

    private final ListeningAudioJobService audioJobService;
    private final ElevenLabsClient elevenLabsClient;

    private static final String HDR_REQ_ID = "X-Request-Id";
    private static final String HDR_IDEMPOTENCY = "Idempotency-Key";

    @Value("${listening.audio.requireIdempotencyKey:true}")
    private boolean requireIdempotencyKey;

    @PostMapping("/audio/generate")
    public ResponseEntity<StartJobResponse> startAudioGeneration(
            @RequestBody AudioGenerateRequest request,
            @RequestParam UUID teacherId,
            @RequestHeader(value = HDR_IDEMPOTENCY, required = false) String idemKey,
            @RequestHeader(value = HDR_REQ_ID, required = false) String requestId
    ) {
        if (requireIdempotencyKey && (idemKey == null || idemKey.isBlank())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StartJobResponse(null, "MISSING_IDEMPOTENCY_KEY"));
        }
        String rid = requestId != null ? requestId : UUID.randomUUID().toString();
        log.info("[{}] Start audio generation requested by teacher {}", rid, teacherId);
        ListeningAudioJob job = audioJobService.startJob(request, idemKey, teacherId, rid);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new StartJobResponse(job.getJobId(), job.getStatus().name()));
    }

    @GetMapping("/audio/jobs/{jobId}")
    public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable UUID jobId) {
        return audioJobService.getJob(jobId)
                .map(job -> ResponseEntity.ok(toResponse(job)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/voices")
    public ResponseEntity<List<VoiceResponse>> listVoices() {
        List<ElevenLabsClient.VoiceInfo> voices = elevenLabsClient.listVoices();
        List<VoiceResponse> resp = voices.stream()
                .map(v -> new VoiceResponse(v.voiceId(), v.name(), v.previewUrl(), v.settings()))
                .toList();
        return ResponseEntity.ok(resp);
    }

    private JobStatusResponse toResponse(ListeningAudioJob job) {
        Instant created = job.getCreatedAt() == null ? null : job.getCreatedAt().toInstant();
        Instant updated = job.getUpdatedAt() == null ? null : job.getUpdatedAt().toInstant();
        return new JobStatusResponse(
                job.getJobId(),
                job.getStatus().name(),
                job.getAudioMaterialId(),
                job.getAudioUrl(),
                job.getDurationSec(),
                job.getTranscriptText(),
                created,
                updated
        );
    }

    public record VoiceResponse(String voiceId, String name, String previewUrl, java.util.Map<String, Object> settings) {}
}

package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.request.TranscriptGenerateRequest;
import com.mytutorplatform.lessonsservice.model.request.TranscriptManualCreateRequest;
import com.mytutorplatform.lessonsservice.model.request.TranscriptUpdateRequest;
import com.mytutorplatform.lessonsservice.model.request.ValidateCoverageRequest;
import com.mytutorplatform.lessonsservice.model.response.TranscriptResponse;
import com.mytutorplatform.lessonsservice.model.response.ValidateCoverageResponse;
import com.mytutorplatform.lessonsservice.service.ListeningTranscriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/listening")
@RequiredArgsConstructor
public class ListeningTranscriptController {

    private final ListeningTranscriptService listeningTranscriptService;

    private static final String HDR_REQ_ID = "X-Request-Id";

    @PostMapping("/transcripts/generate")
    public ResponseEntity<TranscriptResponse> generate(
            @RequestBody TranscriptGenerateRequest request,
            @RequestParam UUID teacherId,
            @RequestHeader(value = HDR_REQ_ID, required = false) String requestId
    ) {
        String rid = requestId != null ? requestId : UUID.randomUUID().toString();
        log.info("[{}] Generate transcript requested by teacher {}", rid, teacherId);
        TranscriptResponse response = listeningTranscriptService.generate(request, teacherId, rid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transcripts/manual")
    public ResponseEntity<TranscriptResponse> createManual(
            @RequestBody TranscriptManualCreateRequest request,
            @RequestParam UUID teacherId
    ) {
        TranscriptResponse response = listeningTranscriptService.createManual(request, teacherId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/transcripts/{transcriptId}")
    public ResponseEntity<TranscriptResponse> update(
            @PathVariable UUID transcriptId,
            @RequestBody TranscriptUpdateRequest request,
            @RequestParam UUID teacherId
    ) {
        TranscriptResponse response = listeningTranscriptService.update(transcriptId, request, teacherId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transcripts/{transcriptId}")
    public ResponseEntity<TranscriptResponse> get(
            @PathVariable UUID transcriptId
    ) {
        TranscriptResponse response = listeningTranscriptService.get(transcriptId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transcripts/validate")
    public ResponseEntity<ValidateCoverageResponse> validateCoverage(
            @RequestBody ValidateCoverageRequest request
    ) {
        ValidateCoverageResponse response = listeningTranscriptService.validateCoverage(request);
        return ResponseEntity.ok(response);
    }
}

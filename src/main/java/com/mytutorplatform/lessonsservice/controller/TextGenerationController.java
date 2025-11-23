package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.request.TextGenerationRequest;
import com.mytutorplatform.lessonsservice.model.response.TextGenerationResponse;
import com.mytutorplatform.lessonsservice.service.TextGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class TextGenerationController {

    private static final String HDR_REQ_ID = "X-Request-Id";

    private final TextGenerationService textGenerationService;

    @PostMapping("/text-blocks")
    public ResponseEntity<TextGenerationResponse> generate(
            @Valid @RequestBody TextGenerationRequest request,
            @RequestParam UUID teacherId,
            @RequestHeader(value = HDR_REQ_ID, required = false) String requestId
    ) {
        String rid = requestId != null ? requestId : UUID.randomUUID().toString();
        log.info("[{}] AI text generation requested by teacher {}", rid, teacherId);
        TextGenerationResponse response = textGenerationService.generate(request, teacherId, rid);
        return ResponseEntity.ok(response);
    }
}

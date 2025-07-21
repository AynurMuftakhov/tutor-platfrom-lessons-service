package com.mytutorplatform.lessonsservice.controller;

import com.mytutorplatform.lessonsservice.model.request.GenerateExerciseRequest;
import com.mytutorplatform.lessonsservice.model.response.GenerateExerciseResponse;
import com.mytutorplatform.lessonsservice.service.ExerciseAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for AI-powered exercise generation endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ExerciseAiController {

    private final ExerciseAiService exerciseAiService;

    /**
     * Endpoint for generating grammar exercises using AI.
     *
     * @param request The request containing exercise generation parameters
     * @return The generated exercise response
     */
    @PostMapping("/exercises")
    public ResponseEntity<GenerateExerciseResponse> generateExercise(
            @Valid @RequestBody GenerateExerciseRequest request) {
        log.info("Received request to generate exercise with grammar focus: {}, topic: {}, level: {}, sentences: {}",
                request.getGrammarFocus(), request.getTopic(), request.getLevel(), request.getSentences());
        
        GenerateExerciseResponse response = exerciseAiService.generateExercise(request);
        
        log.info("Successfully generated exercise with {} sentences", request.getSentences());
        return ResponseEntity.ok(response);
    }
}